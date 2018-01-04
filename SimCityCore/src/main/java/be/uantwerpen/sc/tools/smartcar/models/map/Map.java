package be.uantwerpen.sc.tools.smartcar.models.map;

import be.uantwerpen.sc.tools.smartcar.models.Bot;
import be.uantwerpen.sc.tools.smartcar.models.TrafficLight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Niels on 3/04/2016.
 */
public class Map
{
    private List<Node> nodeList;
    private List<Bot> botEntities;
    private List<TrafficLight> trafficLightEntities;

    public Map()
    {
        nodeList = new ArrayList<>();
        botEntities = new ArrayList<>();
        trafficLightEntities = new ArrayList<>();
    }

    public  void addNode(Node node)
    {
        nodeList.add(node);
    }

    public void setNodeList(List<Node> nodeList)
    {
        this.nodeList = nodeList;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public List<Bot> getBotEntities()
    {
        return botEntities;
    }

    public void setBotEntities(List<Bot> botEntities)
    {
        this.botEntities = botEntities;
    }

    public List<TrafficLight> getTrafficLightEntity()
    {
        return trafficLightEntities;
    }

    public void setTrafficLightEntity(List<TrafficLight> trafficLightEntities)
    {
        this.trafficLightEntities = trafficLightEntities;
    }

    // Used for debugging purposes
    @Override
    public String toString() {
        for(int i = 0; i < 20; i++) {
            System.out.println("AbstractMap{" +
                    "nodeList=" + nodeList.get(i).getNeighbours().get(0).getId() +
                    '}');
        }
        return "AbstractMap{" +
                "nodeList=" + nodeList.get(3).getNeighbours().get(0).getLength() +
                '}';
    }
}
