package be.uantwerpen.sc.services.mapService;

import be.uantwerpen.rc.models.map.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake mapservice to test without RobotBackend functioning
 */

@Profile("mocks")
@Service
public class MapServiceMock extends MapService {

    public MapServiceMock() {
        super();
        logger.warn("Running with mocked MapService. Data will be inaccurate!");
    }

    @Override
    protected Map loadMap() {
        //TODO this mocked map is incomplete, but enough to start application

        Tile tile = new Tile();
        tile.setType("end");
        tile.setLocked(false);

        Point point = new Point();
        point.setTile(tile);
        point.setId(1L);

        Link link = new Link();
        link.getCost().setLength(10);
        link.setStartPoint(point.getId());
        List<Link> neighbours = new ArrayList<>();
        neighbours.add(new Link());

        point.setId(10L);
        point.setNeighbours(neighbours);

        List<Point> nodeList = new ArrayList<>();
        nodeList.add(point);

        Map map = new Map();
        map.setPointList(nodeList);

        return map;
    }

}
