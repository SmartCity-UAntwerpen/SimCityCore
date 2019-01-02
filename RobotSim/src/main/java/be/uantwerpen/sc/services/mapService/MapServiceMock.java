package be.uantwerpen.sc.services.mapService;

import be.uantwerpen.rc.models.map.Map;
import be.uantwerpen.rc.models.map.Node;
import be.uantwerpen.rc.models.map.Point;
import be.uantwerpen.rc.models.map.Tile;
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
        logger.info("Running with mocked MapService. Data will be inaccurate!");
    }

    @Override
    protected Map loadMap() {
        //TODO this mocked map is incomplete, but enough to start application

        Tile tile = new Tile();
        tile.setType("end");

        Point point = new Point();
        point.setTile(tile);

        Node node = new Node();
        node.setPointEntity(point);

        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node);

        Map map = new Map();
        map.setNodeList(nodeList);

        return map;
    }
}
