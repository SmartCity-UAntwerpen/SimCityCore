package be.uantwerpen.sc.services.mapService;

import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public abstract class MapService {

    protected static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private Map map;

    private Queue<Point> startPoints;

    public MapService() {
        startPoints = new LinkedList<>();
    }

    @PostConstruct
    public void init() {
        updateMap();

        // map to list of points
        List<Point> points = map.getPointList().stream()
                .filter(x -> x.getTile().getType().equals("end"))// only start on end-points
                .collect(Collectors.toList());
        startPoints.addAll(points);
    }

    // loading method depends on implementation
    protected abstract Map loadMap();

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
        return map.getPointList().stream()
                .filter(x -> x.getId().equals(point.getId()))
                .collect(Collectors.toList()).get(0).getTileLock();
    }
}
