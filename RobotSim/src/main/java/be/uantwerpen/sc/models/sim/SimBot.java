package be.uantwerpen.sc.models.sim;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Thomas on 03/04/2016.
 * Abstract class for each simulated bot
 * Mainly used for threading
 */
public abstract class SimBot implements Runnable
{
    private Thread simulationThread;
    protected boolean running;

    /**
     * Bot ID
     */
    protected int id;

    /**
     * Bot Type
     */
    protected String type;

    /**
     * Bot Name
     */
    protected String name;

    /**
     * Backend IP
     */
    //@Value("${robotbackend.ip:default}")//TODO Currently doesnt work
    String robotBackendIP="smartcity.ddns.net";

    /**
     * Backend Port
     */
   // @Value("#{new Integer(${robotbackend.port})}")//TODO currently doesnt work
    int robotBackendPort=8083;

    protected SimBot()
    {
        this.running = false;
        this.type = "bot";
        this.name = "SimBot";
    }

    public SimBot(String name)
    {
        this();
        this.name = name;
    }

    /**
     * Starts bot
     * @return Success
     */
    public boolean start()
    {
        if(this.running)
            return false;

        this.simulationThread = new Thread(this);
        this.simulationThread.start();
        this.running = true;

        if(this.getStartPoint() == -1)
            return false;
        return true;
    }

    public boolean restart()
    {
        if(simulationThread != null) {
            if (this.running)
                this.running = false;

            while (simulationThread.isAlive()); //Wait for thread to stop
        }
        return this.start();
    }

    public boolean stop()
    {
        if(!this.running || this.simulationThread == null)
            return false;

        if(this.running) {
            this.running = false;
            return true;
        }
        return false;
    }

    public boolean remove()
    {
        if(this.running) {
            this.running = false;
        }
        return true;
    }

    public boolean interrupt()
    {
        if(this.simulationThread == null)
            return false;

        if(this.simulationThread.isAlive()) {
            this.simulationThread.interrupt();
            this.running = false;
        }

        return true;
    }

    public boolean isRunning()
    {
        return this.running;
    }

    public String getType()
    {
        return this.type;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public abstract int getStartPoint();

    public boolean parseProperty(String property, String value) throws Exception
    {
        switch(property.toLowerCase().trim())
        {
            case "name":
                setName(value);
                return true;
            default:
                return false;
        }
    }

    public boolean parseProperty(String property) throws Exception
    {
        switch(property.toLowerCase().trim())
        {
            case "name":
                return true;
            default:
                return false;
        }
    }

    public boolean printProperty(String property)
    {
        switch(property.toLowerCase().trim())
        {
            case "name":
                System.out.println(name + "\n");
                return true;
            default:
                return false;
        }
    }

    @Override
    public void run()
    {
        while(this.running)
        {
            this.simulationProcess();
        }
    }

    abstract protected void simulationProcess();
}