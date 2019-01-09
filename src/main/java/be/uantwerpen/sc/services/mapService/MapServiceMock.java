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
        link.setLength(10);
        link.setStartPoint(point);
        List<Link> neighbours = new ArrayList<>();
        neighbours.add(new Link());

        Node node = new Node();
        node.setPointEntity(point);
        node.setNodeId(10L);
        node.setNeighbours(neighbours);

        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node);

        Map map = new Map();
        map.setNodeList(nodeList);

        return map;
    }

}
