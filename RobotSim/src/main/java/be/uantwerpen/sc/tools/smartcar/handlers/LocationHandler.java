package be.uantwerpen.sc.tools.smartcar.handlers;

import be.uantwerpen.rc.models.map.Link;
import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Node;
import be.uantwerpen.sc.configurations.SpringContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Iterator;

/**
 * Created by Thomas on 28/05/2016.
 * Handles bot location
 */
public class LocationHandler
{
    private long currentLocation;
    private long destinationLocation;
    private double destinationDistance;
    /**
     * TODO Map per locationhandler useful?
     */
    private Map map;
    private boolean onMap;
    private double currentDirection;
    private double destinationDirection;
    private boolean followLine;

    /**
     * Backend IP
     */
    private String robotBackendIP;
    /**
     * Backend Port
     */
    private int robotBackendPort;

    public LocationHandler()
    {
        this.currentLocation = -1;
        this.destinationLocation = -1;
        this.destinationDistance = 0L;
        this.map = null;
        this.onMap = false;
        this.currentDirection = 0;
        this.destinationDirection = 0;
        this.followLine = false;

        //Get values from spring
        ApplicationContext context =  SpringContext.getAppContext();
        this.robotBackendIP = context.getEnvironment().getProperty("robotbackend.ip");
        String portString = context.getEnvironment().getProperty("robotbackend.port");
        this.robotBackendPort = Integer.parseInt(portString);
    }

    /**
     * Gets map and fills position and directions
     * @param startPosition ID of start node
     * @return Success
     */
    public boolean initLocationHandler(int startPosition)
    {
        try {
            this.map = this.getMap();
        }
        catch(Exception e) {
            System.err.println("Could not connect to SmartCity Core for map!");
            e.printStackTrace();
            return false;
        }

        if(this.map == null) {
            System.err.println("Could not retrieve map from SmartCity Core!");
            return false;
        }

        //Find start node if existing
        boolean found = false;
        Iterator<Node> it = this.map.getNodeList().iterator();
        Node startNode = null;

        while(it.hasNext() && ! found) {
            Node node = it.next();
            if(node.getNodeId() == startPosition) {
                startNode = node;
                found = true;
            }
        }

        //Node needs at least one neighbour to navigate
        if(found && startNode.getNeighbours().size() > 0)
        {
            //Determine destination location (bot starts on edge point)
            this.currentDirection = startNode.getNeighbours().get(0).getAngle();

            //Destination location (look direction) is opposite of absolute drive in direction
            this.destinationDirection = startNode.getNeighbours().get(0).getAngle() % 360;

            this.destinationLocation = startNode.getNeighbours().get(0).getEndPoint().getId();
            this.destinationDistance = startNode.getNeighbours().get(0).getWeight(); //FIXME can weight be used instead of length?

            this.currentLocation = startPosition;
        }
        else {
            System.err.println("Could not find start position for id " + startPosition + "!");
            return false;
        }

        this.onMap = true;

        return true;
    }

    public void removedFromMap()
    {
        this.onMap = false;
    }

    public boolean onMap()
    {
        return this.onMap;
    }

    /**
     * Checks if the vehicle is on a node
     * @return True/False
     */
    public boolean onNode()
    {
        //TODO
        return false;
    }

    /**
     * Gets distance from location
     * @return Distance
     */
    public int getDistanceTargetLocation()
    {
        return (int)this.destinationDistance;
    }

    /**
     * Gets robot current location
     * @return ID of node position TODO
     */
    public int getCurrentLocation()
    {
        return (int) this.currentLocation;
    }

    /**
     * Gets RFID of given node
     * @param nodeID Id of node
     * @return RFID
     */
    public String getNodeRFID(int nodeID)
    {
        if(map == null)
            return null; //No map loaded

        for(Node node : this.map.getNodeList())
        {
            if(node.getNodeId() == nodeID)
                return node.getPointEntity().getTile().getRfid();
        }
        return null;
    }

    /**
     * Starts bot following a simulated line
     */
    // Hoofdtaak is bepalen over welke afstand de lijn doorloopt
    public void startFollowLine()
    {
        Node currentNode = null;
        if(map == null)
            return; //No map loaded

        boolean foundNode = false;
        Iterator<Node> itNode = this.map.getNodeList().iterator();

        //Search current node
        while(itNode.hasNext() && !foundNode)
        {
            Node node = itNode.next();
            if(node.getNodeId() == this.currentLocation)
            {
                currentNode = node;
                foundNode = true;
            }
        }

        if(foundNode)
        {
            boolean foundLink = false;
            Iterator<Link> itLink = currentNode.getNeighbours().iterator();
            Link followLink = null;

            while(itLink.hasNext() && !foundLink) {
                Link link = itLink.next();

                double direction = link.getAngle();
                if(direction == this.currentDirection)
                {
                    followLink = link;
                    foundLink = true;
                }
            }

            if(foundLink) {
                //Destination location (look direction) is opposite of absolute drive in direction
                this.destinationDirection = followLink.getAngle() + 180;

                this.destinationLocation = followLink.getEndPoint().getId();
                this.destinationDistance = followLink.getWeight(); // FIXME should be length!

                this.followLine = true;
            }
            else
                this.onMap = false; //Could not find destination, going off road
        }
        else
            this.onMap = false; //Could not find node

        return;
    }

    /**
     * Ends line following, arrived at destination
     */
    public void endFollowLine()
    {
        if(map == null)
            return;//No map loaded

        if(followLine)
        {
            this.currentLocation = this.destinationLocation;
            this.currentDirection = this.destinationDirection;
            this.followLine = false;
        }
    }

    /**
     * Rotates vehicle with specified angle and sets the current direction based on that
     * @param angle Angle to rotate
     */
    public void turn(double angle)
    {
        this.currentDirection += ((int)angle) % 360;
    }


    /**
     * Gets map from Robot Backend
     * @return received map
     */
    private Map getMap()
    {
        RestTemplate template = new RestTemplate();
        ResponseEntity<Map> responseMap;
        Map map;

        responseMap = template.getForEntity("http://" + this.robotBackendIP + ":" + String.valueOf(this.robotBackendPort) + "/map/", Map.class);
        map = responseMap.getBody();

        return map;
    }
}
