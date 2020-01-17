package be.uantwerpen.sc.models.sim.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TODO
 */
@Deprecated
public class TCPUtils extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(TCPUtils.class);

    private ServerSocket serverSocket;
    private Socket socket;
    private int clientPort;
    private int serverPort;
    private BufferedReader in;
    private PrintWriter out;
    private TCPListener listener;
    private boolean ackNack;

    public TCPUtils(int port, TCPListener listener,boolean ackNack) throws IOException {
        serverSocket = new ServerSocket(port);
        this.listener = listener;
        this.ackNack = ackNack;
    }

    public TCPUtils(int clientPort, int serverPort, TCPListener listener,boolean ackNack){
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.listener = listener;
        this.ackNack = ackNack;
    }

    public void run() {
        if(ackNack) {
            while (true) {
                try {
                    Socket server = serverSocket.accept();

                    try {
                        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                        out = new PrintWriter(server.getOutputStream(), true);
                    } catch (IOException e) {
                        logger.error("Cannot receive data." + e);
                        //Log.logSevere("SOCKETS", "Cannot receive data." + e);
                    }
                    while (true) {
                        try {
                            //Send data back to client
                            String data = in.readLine();
                            logger.debug("Data received: " + data);
                            //Log.logConfig("SOCKETS", "data received: " + data);
                            String response = listener.parseTCP(data);
                            out.println(response);
                            logger.debug("Data Sent:" + response);
                            //Log.logConfig("SOCKETS", "Data Sent:" + response);
                            break;
                        } catch (IOException e) {
                            logger.error("Cannot receive data." + e);
                            //Log.logSevere("SOCKETS", "Cannot receive data." + e);
                        }
                    }
                    server.close();

                } catch (SocketTimeoutException s) {
                    logger.error("Timed out." + s);
                    //Log.logSevere("SOCKETS", "Timed out." + s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            ServerSocket echoServer = null;
            String line;
            DataInputStream is;
            try {
                echoServer = new ServerSocket(serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean run = true;
            while (run) {
                try {
                    if (echoServer != null) {
                        socket = echoServer.accept();
                    }
                    is = new DataInputStream(socket.getInputStream());

                    line = is.readLine();
                    if(line != null ){
                        logger.debug("Data received: " + line);
                        //Log.logConfig("SOCKETS","data received: " + line);
                        listener.parseTCP(line);
                    }
                } catch (IOException e) {
                    logger.error("Cannot receive data." + e);
                    //Log.logSevere("SOCKETS","Cannot receive data." + e);
                }
            }
            closeTCP();
        }
    }


    /**
     * Tends message over clientSocket to vehicle
     * @param data message to send
     */
    public void sendUpdate(String data) {
        Socket clientSocket = null;
        DataInputStream inputLine = new DataInputStream(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        byte[] bytes = new byte[100];
        Arrays.fill(bytes, (byte) 1);

        boolean connected = false;
        //if connection to socket can not be made, it waits until it can.
        while (!connected) {
            try {
                clientSocket = new Socket("localhost", clientPort);

                connected = true;
            } catch (UnknownHostException e) {
                logger.error("Cannot connect to receiver. Trying again." + e);
                //Log.logSevere("SOCKETS","Cannot connect to receiver. Trying again." + e);
                connected = false;
            } catch (IOException e) {
                logger.warn("Cannot connect to receiver to send   " + data + "   Trying again. Error:" + e);
                //Log.logWarning("SOCKETS","Cannot connect to receiver to send   " + data + "   Trying again. Error:" + e);
                connected = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            PrintStream os = new PrintStream(clientSocket.getOutputStream());
            os.println(inputLine.readLine());
            logger.debug("Data Sent:" + data);
            //Log.logConfig("SOCKETS","Data Sent:" + data);
            os.close();
        } catch (UnknownHostException e) {
            logger.warn("Could not send. Trying to connect to unknown host: " + e);
            //Log.logWarning("SOCKETS","Could not send. Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            logger.error("Could not send. IOException:  " + e);
            //Log.logSevere("SOCKETS","Could not send. IOException:  " + e);
        }
    }

    /**
     * Closes TCP connection
     */
    public void closeTCP(){
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Could not close Socket connection. IOException:  " + e);
            //Log.logSevere("SOCKETS","Could not close Socket connection. IOException:  " + e);
        }
    }
}