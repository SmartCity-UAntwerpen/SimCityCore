package be.uantwerpen.sc.tools.smartcar.handlers;

import be.uantwerpen.rc.models.map.Link;
import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Node;
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
    private static final Logger logger = LoggerFactory.getLogger(LocationHandler.class);

    private Point currentLocation;
    private Point destinationLocation;
    private double destinationDistance;
    private boolean driving = false;
    private boolean followline = false;

    private Point rollbackLocation;
    private List<String> prevCommandBuffer;

    private static Map map = null;
    private MapService mapService;

    public LocationHandler()
    {
        this.currentLocation = null;
        this.destinationLocation = null;
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
        Iterator<Node> it = map.getNodeList().iterator();
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
            this.destinationLocation = startNode.getNeighbours().get(0).getEndPoint();
            this.destinationDistance = startNode.getNeighbours().get(0).getLength();

            this.currentLocation = startNode.getPointEntity();
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

        for(Node node : map.getNodeList())
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

        Node currentNode = findNodeByPointId(currentLocation.getId());
        if(currentNode == null) {
            logger.warn("Couldn't find node for point!");
            return;
        }
        Iterator<Link> linkIt = currentNode.getNeighbours().iterator();

        Link futureLink = null;

        while(linkIt.hasNext() && futureLink == null) {
            Link link = linkIt.next();

            if(link.getStartPoint().getId().equals(currentLocation.getId())) {
                //Link has correct direction
                futureLink = link;
            }
        }

        if(futureLink == null) {
            logger.warn("Couldn't find a direction to follow line!");
            return;
        }

        logger.info("following link from "+futureLink.getStartPoint().getId()+" to "+futureLink.getEndPoint().getId()+" over distance "+futureLink.getLength());

        driving = true;
        this.destinationDistance = futureLink.getLength();
        this.destinationLocation = futureLink.getEndPoint();
        this.followline = true;
    }

    public void updatePosTurn(float angle) {
        angle = -angle; // map and other parts of the simulator are vice versa :(

        if(!(currentLocation.getTile().getType().equals("crossing") || followline )) {
            logger.info("Drive command ignored");
            return; // Ignore manual drive commands except for crossing or line following
        }

        Node currentNode = findNodeByPointId(currentLocation.getId());
        if(currentNode == null) return;

        for(Link link: currentNode.getNeighbours()) {
            if((link.getAngle() == angle) && (currentLocation.getId().equals(link.getStartPoint().getId()))) {
                // Link angle is correct and we are at the start of it
                // Theoretically, the robot could drive in the wrong direction. This is not implemented
                destinationLocation = link.getEndPoint();
                destinationDistance = link.getLength();
                driving = true;

                logger.info("Driving distance "+destinationDistance+" with angle "+angle+" to point "+destinationLocation.getId());
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
        this.currentLocation = this.destinationLocation;
        this.destinationDistance = 0;
        this.followline = false;
    }

    // search point by id
    private Point findPointById(long id) {
        Node node =findNodeByPointId(id);
        if(node != null) return node.getPointEntity();
        else return null;
    }

    // search node by it's contained point id
    private Node findNodeByPointId(long pid) {
        List<Node> nodes = map.getNodeList();

        for(Node node : nodes) {
            Point point = node.getPointEntity();
            long currentId = point.getId();
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
