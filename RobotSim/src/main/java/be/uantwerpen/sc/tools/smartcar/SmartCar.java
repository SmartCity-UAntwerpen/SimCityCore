package be.uantwerpen.sc.tools.smartcar;

import be.uantwerpen.sc.services.sockets.SimSocketService;
import be.uantwerpen.sc.services.sockets.SimSocket;
import be.uantwerpen.sc.tools.smartcar.handlers.*;

/**
 * Created by Thomas on 28/05/2016.
 */
public class SmartCar
{
    private final static String version = "0.0.1";
    private SimSocket taskSocket;
    private SimSocket eventSocket;
    private String name;
    private DriveHandler driveHandler;
    private TaskHandler taskHandler;
    private EventHandler eventHandler;
    private LocationHandler locationHandler;
    private TagReaderHandler tagReaderHandler;

    public SmartCar()
    {
        this.taskSocket = null;
        this.eventSocket = null;
        this.name = "SimBot";

        this.eventHandler = new EventHandler();
        this.locationHandler = new LocationHandler();
        this.driveHandler = new DriveHandler(70, locationHandler);
        this.tagReaderHandler = new TagReaderHandler(locationHandler);
        this.taskHandler = new TaskHandler(driveHandler, eventHandler, locationHandler, tagReaderHandler);
    }

    public SmartCar(String name, float speed)
    {
        this();
        this.name = name;
        this.driveHandler.setSpeed(speed);
    }


    public String getVersion()
    {
        return this.version;
    }

    /**
     * Initializes simulation by initializing the location handler
     * @param startPosition ID start position
     * @return Success
     */
    public boolean initSimulation(int startPosition)
    {
        return locationHandler.initLocationHandler(startPosition);
    }

    /**
     * Shuts down simulation
     * @return Success
     */
    public boolean stopSimulation()
    {
        if(this.taskSocket != null)
        {
            try {
                this.taskSocket.close();
            }
            catch(Exception e) {
                System.err.println("Could not close task socket of terminated simulation!");
            }
        }

        if(this.eventSocket != null) {
            try {
                this.eventSocket.close();
            }
            catch(Exception e) {
                System.err.println("Could not close event socket of terminated simulation!");
            }
        }

        return true;
    }

    public void checkConnections(SimSocketService taskSocketService, SimSocketService eventSocketService)
    {
        boolean socketReset = false;

        //Verify task socket
        if(taskSocket != null)
        {
            if(taskSocket.isClosed())
                socketReset = true;
        }
        else
            socketReset = true;

        if(socketReset) {
            //Get new connection
            taskSocket = taskSocketService.getConnection();

            if(taskSocket != null) {
                taskSocket.sendMessage("SmartCity Car: " + this.name + " - Version: " + this.version + "\r\n# "); //Send init message
            }
        }

        //Verify event socket
        socketReset = false;

        if(eventSocket != null) {
            if (eventSocket.isClosed())
                socketReset = true;
        }
        else
            socketReset = true;

        if(socketReset)
        {
            //Get new connection
            eventSocket = eventSocketService.getConnection();

            if(eventSocket != null)
                eventSocket.sendMessage("SmartCity Car: " + this.name + " - Version: " + this.version + "\r\n"); //Send init message
        }
    }

    public void updateSimulation(double elapsedTime)
    {
        //Update drive module
        if(driveHandler.updatePosition(elapsedTime))
        {
            //Target position reached, generate event
            eventHandler.addEvent("DRIVE EVENT: FINISHED");
            locationHandler.drivingDone();
        }

        //Process available tasks
        if(this.taskSocket != null)
            taskHandler.processMessage(this.taskSocket);

        //Process available events
        if(this.eventSocket != null)
            eventHandler.processEvents(this.eventSocket);
        else
            this.eventHandler.flushEvents();    //Flush generated events to simulate the disabled event generator

        //Start next drive task if available and not driving
        driveHandler.startNextDriveTask();
    }
}
