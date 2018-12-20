package be.uantwerpen.sc.services;

import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Node;
import be.uantwerpen.rc.models.map.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
public class MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private Map map;

    private Queue<Point> startPoints;

    /**
     * Backend IP
     */
    @Value("${robotbackend.ip}")
    private String robotBackendIP;
    /**
     * Backend Port
     */
    @Value("#{new Integer(${robotbackend.port})}")
    private int robotBackendPort;

    public MapService() {
        startPoints = new LinkedList<>();
    }

    @PostConstruct
    public void init() {
        updateMap();

        // map to list of points
        List<Point> points = map.getNodeList().stream()
                .map(Node::getPointEntity)
                .filter(x -> x.getTile().getType().equals("end"))// only start on end-points
                .collect(Collectors.toList());
        startPoints.addAll(points);
    }

    /**
     * Gets map from Robot Backend
     * @return received map
     */
    private Map loadMap()
    {
        RestTemplate template = new RestTemplate();
        ResponseEntity<Map> responseMap;
        Map map;

        responseMap = template.getForEntity("http://" + this.robotBackendIP + ":" + String.valueOf(this.robotBackendPort) + "/map/", Map.class);
        map = responseMap.getBody();

        logger.info("Map loaded OK");
        return map;
    }

    public Map getMap() {
        return map;
    }

    public Queue<Point> getStartPoints() {
        if(startPoints.size() == 0) init(); // reload when empty
        return startPoints;
    }

    public void updateMap() {
        logger.info("Updating robot map from backend.");
        map = loadMap();
    }

    public boolean checkPointLock(Point point) {
        return map.getNodeList().stream()
                .map(Node::getPointEntity)
                .filter(x -> x.getId().equals(point.getId()))
                .collect(Collectors.toList()).get(0).getTileLock();
    }
}
