package be.uantwerpen.sc.models.sim;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Thomas on 5/05/2017.
 * Core simulation
 */
public class SimCore
{
    /**
     * Robot core jar location
     */
    private String coreLocation;

    /**
     * SimCore version
     */
    private String version;

    /**
     * Status of the simulation
     */
    private SimStatus status;

    /**
     * Core thread running
     */
    private boolean running;

    /**
     * Arguments for the Robot Core (probably)
     */
    private List<String> runArguments;

    /**
     * Thread running the core
     */
    private Thread coreThread;

    private SimCore() {
        this.coreLocation = "";
        this.version = "0.0.0";
        this.status = SimStatus.OFF;
        this.running = false;
        this.coreThread = null;
    }

    public SimCore(String coreLocation, String version) {
        this();
        this.coreLocation = coreLocation;
        this.version = version;
    }

    public String getCoreLocation()
    {
        return this.coreLocation;
    }

    public String getVersion() { return this.version; }

    public SimStatus getStatus()
    {
        return this.status;
    }

    /**
     * Start running the core in the thread
     * @param arguments
     * @return Success (false if already running)
     */
    public boolean start(List<String> arguments)
    {
        if(!running)
        {
            this.runArguments = arguments;

            coreThread = new Thread(new CoreProcess());
            System.out.println("Starting JAR-file thread...");
            coreThread.start();

            running = true;
            return true;
        }
        else
            return false;
    }

    public boolean stop()
    {
        if(running)
        {
            coreThread.interrupt();
            return true;
        }
        else
            return false;
    }

    //TODO change to redirect output to file instead of stdout
    private class CoreProcess implements Runnable
    {
        /**
         * Runs Core jar with given arguments
         */
        @Override
        public void run()
        {
            //Create process
            ProcessBuilder processBuilder = new ProcessBuilder("java");

            //Add core boot arguments
            List<String> processCommands = processBuilder.command();
            processCommands.addAll(runArguments);
            processCommands.add("-jar");
            processCommands.add(coreLocation);
            processBuilder.command(processCommands);

            //Uncomment these lines to test with core in IDE
            //status = SimStatus.RUNNING;
            //while(!Thread.currentThread().isInterrupted());

            status = SimStatus.BOOT;

            Process process;
            try {
                process = processBuilder.start();
            }
            catch(Exception e)
            {
                System.err.println("Could not create Core process!");
                e.printStackTrace();

                status = SimStatus.ERROR;

                running = false;

                return;
            }

            // Get stdin of JAR = outputstream of our app
            OutputStream stdin = process.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String logLine = "";
            String errorLine = "";

            while(!Thread.currentThread().isInterrupted() && !logLine.startsWith("SmartCar Core")) // wait until initialized
            {
                try {
                    logLine = readLine(reader);

                    if(logLine == null)
                        break; //Input stream closed, exit boot status

                    while(errorReader.ready() && !Thread.currentThread().isInterrupted() && errorLine != null)
                    {
                        errorLine = readLine(errorReader);
                    }
                }
                catch(IOException e)
                {
                    if(!Thread.currentThread().isInterrupted())
                    {
                        System.err.println("Could not open input stream!");
                        e.printStackTrace();

                        status = SimStatus.ERROR;
                        process.destroy();
                        running = false;
                        return;
                    }
                    else
                        break; //System interrupted
                }
            }

            System.out.println("Car core initialized. Status: running");
            status = SimStatus.RUNNING;

            try
            {
                while(!Thread.currentThread().isInterrupted() && logLine != null)
                {
                    logLine = readLine(reader);

                    while(errorReader.ready() && !Thread.currentThread().isInterrupted() && errorLine != null)
                    {
                        errorLine = readLine(errorReader);
                    }
                }
            }
            catch(IOException e)
            {
                if(!Thread.currentThread().isInterrupted())
                {
                    System.err.println("Could not read input stream!");
                    e.printStackTrace();

                    logLine = null;
                }
            }

            if(logLine == null) {
                System.out.println("Simulation stopped unexpected");
                status = SimStatus.ERROR;
                process.destroy();
                running = false;
                return;
            }

            //Send shutdown signal to process
            status = SimStatus.SHUTDOWN;

            System.out.println("Shutting down simulation");
            try {
                // send exit command for clean shutdown
                writer.write("exit\n");
                writer.flush();
            } catch (IOException e) {
                System.err.println("Could not send shutdown command. Force shutdown.");

                running = false;
                status = SimStatus.ERROR;

                process.destroy();
                e.printStackTrace();
                return;
            }

            //Reset cached lines
            logLine = "";
            errorLine = "";

            //Wait for core process to shutdown
            try
            {
                while(logLine != null)
                {
                    logLine = readLine(reader);

                    while(errorReader.ready() && errorLine != null)
                    {
                        errorLine = readLine(errorReader);
                    }
                }
            }
            catch(IOException e) {
                if(!Thread.currentThread().isInterrupted())
                {
                    System.err.println("Could not read input stream!");
                    e.printStackTrace();

                    status = SimStatus.ERROR;
                    process.destroy();
                    running = false;
                    return;
                }
            }

            try {
                reader.close();
            }
            catch(IOException e) {
                System.err.println("Could not close input stream!");
            }

            try {
                errorReader.close();
            }
            catch(IOException e) {
                System.err.println("Could not close error stream!");
            }

            System.out.println("Simulation stopped");
            status = SimStatus.OFF;
            running = false;
        }
    }

    private String readLine(BufferedReader reader) throws IOException
    {
        final String[] line = {null};
        final boolean[] exFlag = {false};
        final String[] exMessage = {null};
        Thread readThread;

        if(reader != null)
        {
            readThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        line[0] = reader.readLine();
                    }
                    catch(IOException e) {
                        //Could not open stream
                        exFlag[0] = true;
                        exMessage[0] = e.getMessage();
                    }
                }
            });

            //Start thread
            readThread.start();

            //Wait for thread to finish or until thread is interrupted
            while(readThread.isAlive() && !Thread.currentThread().isInterrupted());

            if(Thread.currentThread().isInterrupted())
                readThread.interrupt(); //Interrupt read thread
            else
            {
                if(exFlag[0])
                    throw new IOException(exMessage[0]); //IOException occurred during reading
            }
        }

        if(line[0] != null) {
            System.out.println("#Core: "+line[0]);
        }

        return line[0];
    }
}
