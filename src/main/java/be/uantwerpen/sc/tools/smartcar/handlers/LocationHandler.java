package be.uantwerpen.sc.tools.smartcar.handlers;

import be.uantwerpen.rc.models.map.Link;
import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Point;
import be.uantwerpen.sc.configurations.SpringContext;
import be.uantwerpen.sc.services.mapService.MapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas on 28/05/2016.
 * Handles bot location
 */
public class LocationHandler
{
    protected static final Logger logger = LoggerFactory.getLogger(LocationHandler.class);

    private Point currentLocation;
    private Long destinationLocation;
    protected double destinationDistance;
    protected boolean driving = false;
    protected boolean followline = false;

    private Point rollbackLocation;
    private List<String> prevCommandBuffer;

    private static Map map = null;
    private MapService mapService;

    public LocationHandler()
    {
        this.currentLocation = null;
        this.destinationLocation = 0L;
        this.destinationDistance = 0L;
        prevCommandBuffer = new ArrayList<>();
        rollbackLocation = null;

        //Get values from spring
        ApplicationContext context =  SpringContext.getAppContext();
        this.mapService = context.getBean(MapService.class);
    }

    /**
     * Gets map and fills position and directions
     * @param startPosition ID of start node
     * @return Success
     */
    public boolean initLocationHandler(int startPosition)
    {
        try {
            map = mapService.getMap();
        }
        catch(Exception e) {
            System.err.println("Could not connect to SmartCity Core for map!");
            e.printStackTrace();
            return false;
        }

        if(map == null) {
            System.err.println("Could not retrieve map from SmartCity Core!");
            return false;
        }

        //Find start node if existing
        boolean found = false;
        Iterator<Point> it = map.getPointList().iterator();
        Point startNode = null;

        while(it.hasNext() && !found) {
            Point node = it.next();
            if(node.getId() == startPosition) {
                startNode = node;
                found = true;
            }
        }

        //Node needs at least one neighbour to navigate
        if(found && startNode.getNeighbours().size() > 0)
        {
            this.destinationLocation = startNode.getNeighbours().get(0).getEndPoint();
            this.destinationDistance = startNode.getNeighbours().get(0).getCost().getLength();

            this.currentLocation = startNode;
        }
        else {
            System.err.println("Could not find start position for id " + startPosition + "!");
            return false;
        }


        return true;
    }

    /**
     * Checks if the vehicle is on a node
     * @return True/False
     */
    public boolean onNode()
    {
        if(currentLocation == null) return false;
        else if(driving) return false; // we are not stopped on a node
        else return true; // we are stopped on a node
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
        return (int) (long) this.currentLocation.getId();
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

        for(Point node : map.getPointList())
        {
            if(node.getId() == nodeID) {
                String rfid = node.getTile().getRfid();
                if(rfid == null || rfid.equals("")) logger.warn("Empty RFID returned for node "+nodeID);
                return rfid;
            }
        }
        logger.warn("No RFID tag found for node "+nodeID);
        return null;
    }

    /**
     * Starts bot following a simulated line
     */
    // Hoofdtaak is bepalen over welke afstand de lijn doorloopt
    public void startFollowLine()
    {
        if(currentLocation.getTile().getType().equals("end"))
                prevCommandBuffer.add("FOLLOWEND");
        else {
            prevCommandBuffer.add("FOLLOW");
            if(detectStartSequence()) {
                if(rollbackLocation == null) logger.warn("Cannot roll back location!");
                else {
                    logger.info("Rolling back to point "+rollbackLocation.getId());
                    currentLocation =  rollbackLocation; // roll back to before start sequence
                }
            }
        }
        rollbackLocation = currentLocation;

        logger.info("Starting followline command");
        if(map == null)
            return; //No map loaded

        Point currentNode = findNodeByPointId(currentLocation.getId());
        if(currentNode == null) {
            logger.warn("Couldn't find node for point!");
            return;
        }
        Iterator<Link> linkIt = currentNode.getNeighbours().iterator();

        Link futureLink = null;

        while(linkIt.hasNext() && futureLink == null) {
            Link link = linkIt.next();

            if(link.getStartPoint().equals(currentLocation.getId())) {
                //Link has correct direction
                futureLink = link;
            }
        }

        if(futureLink == null) {
            logger.warn("Couldn't find a direction to follow line!");
            return;
        }

        logger.info("following link from "+futureLink.getStartPoint()+" to "+futureLink.getEndPoint()+" over distance "+futureLink.getCost().getLength());

        driving = true;
        this.destinationDistance = futureLink.getCost().getLength();
        this.destinationLocation = futureLink.getEndPoint();
        this.followline = true;
    }

    public void updatePosTurn(float angle) {
        angle = -angle; // map and other parts of the simulator are vice versa :(

        if(!(currentLocation.getTile().getType().equals("crossing") || followline )) {
            logger.info("Drive command ignored");
            return; // Ignore manual drive commands except for crossing or line following
        }

        Point currentNode = findNodeByPointId(currentLocation.getId());
        if(currentNode == null) return;

        for(Link link: currentNode.getNeighbours()) {
            if((link.getAngle() == angle) && (currentLocation.getId().equals(link.getStartPoint()))) {
                // Link angle is correct and we are at the start of it
                // Theoretically, the robot could drive in the wrong direction. This is not implemented
                destinationLocation = link.getEndPoint();
                destinationDistance = link.getCost().getLength();
                driving = true;

                logger.info("Driving distance "+destinationDistance+" with angle "+angle+" to point "+destinationLocation);
                return;
            }
        }
        logger.warn("Error finding link for turn command with angle "+angle+"° at point "+currentLocation.getId());
    }

    public void updatePosDrive() {
        prevCommandBuffer.add("FORWARD");

        // we assume the robot is driving to the following point
        // This is equivalent to the turn command with an angle of 0
        updatePosTurn(0);
    }

    /**
     *   Arrived at destination
     */
    public void drivingDone() {
        logger.info("Driving command completed");
        driving = false;
        this.currentLocation.setId(this.destinationLocation);
        this.destinationDistance = 0;
        this.followline = false;
    }

    // search point by id
    private Point findPointById(long id) {
        Point node =findNodeByPointId(id);
        if(node != null) return node;
        else return null;
    }

    // search node by it's contained point id
    private Point findNodeByPointId(long pid) {
        List<Point> nodes = map.getPointList();

        for(Point node : nodes) {
            long currentId = node.getId();
            if(currentId == pid) return node;
        }

        return null;
    }

    /**
     * Detect sequence at start of job which lead to a single followline
     */
    private boolean detectStartSequence() {
        if(prevCommandBuffer.size() < 4) return false; // minimum 3 commands

        boolean success = true;

        logger.info("Command buffer: "+prevCommandBuffer);

        if (!prevCommandBuffer.get(0).equals("FOLLOWEND")) success = false;
        if (!prevCommandBuffer.get(1).equals("FORWARD")) success = false; // this forward command is the result of the followline
        if (!prevCommandBuffer.get(2).equals("FORWARD")) success = false;
        if (!prevCommandBuffer.get(3).equals("FOLLOW")) success = false;

        if (success) logger.info("Start sequence detected!");

        prevCommandBuffer.clear();

        return success;
    }

}
