package be.uantwerpen.sc.tools.smartcar.handlers;

import be.uantwerpen.rc.models.map.Link;
import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Node;
import be.uantwerpen.rc.models.map.Point;
import be.uantwerpen.sc.configurations.SpringContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas on 28/05/2016.
 * Handles bot location
 */
public class LocationHandler
{
    private Point currentLocation;
    private Point destinationLocation;
    private double destinationDistance;
    /**
     * TODO Map per locationhandler useful?
     */
    private Map map;
    private boolean onMap;
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
        this.currentLocation = null;
        this.destinationLocation = null;
        this.destinationDistance = 0L;
        this.map = null;
        this.onMap = false;
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
            this.destinationLocation = startNode.getNeighbours().get(0).getEndPoint();
            this.destinationDistance = startNode.getNeighbours().get(0).getLength();

            this.currentLocation = findPointById(startPosition);
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
        if(map == null)
            return; //No map loaded

        Node currentNode = findNodeByPointId(currentLocation.getId());
        if(currentNode == null) {
            System.err.println("Couldn't find node for point!");
            return;
        }
        Iterator<Link> linkIt = currentNode.getNeighbours().iterator();

        Link futureLink = null;

        while(linkIt.hasNext() && futureLink == null) {
            Link link = linkIt.next();

            if(link.getStartPoint().equals(currentLocation)) {
                //Link has correct direction
                futureLink = link;
            }
        }

        if(futureLink == null) {
            System.out.println("Couldn find a direction to follow line!");
            this.onMap = false;
            return;
        }

        this.destinationDistance = futureLink.getLength();
        this.destinationLocation = futureLink.getEndPoint();
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
            this.followLine = false;
        }
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
}
