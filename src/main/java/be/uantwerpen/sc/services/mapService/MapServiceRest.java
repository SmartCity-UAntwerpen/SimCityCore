package be.uantwerpen.sc.services.mapService;

import be.uantwerpen.rc.models.map.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of the mapService that uses the REST API to get it's map
 */

// Only use when mock isn't
@Profile("!mocks")
@Service
public class MapServiceRest extends MapService {

    /**
     * Backend IP
     */
    @Value("${robotbackend.ip}")
    private String robotBackendIP;
    /**
     * Backend Port
     */
    @Value("#{new Integer(1994)}")
    private int robotBackendPort;

    /**
     * Gets map from Robot Backend
     * @return received map
     */
    protected Map loadMap()
    {
        RestTemplate template = new RestTemplate();
        ResponseEntity<Map> responseMap;
        Map map;

        try {
            responseMap = template.getForEntity("http://" + this.robotBackendIP + ":" + this.robotBackendPort + "/map/", Map.class);
            logger.info("map loaded?");
            map = responseMap.getBody();
            logger.info("Map loaded OK");
            logger.info("Map received: " +map);
            logger.info("Test sample from map: " +map.getPointList().get(0).getNeighbours().get(0).getStartPoint());
        }
        catch (ResourceAccessException e) {
            logger.error("Error loading map from backend. Check connection and settings!");
            throw e; // rethrow to stop application and print stacktrace
        }

        return map;
    }
}
