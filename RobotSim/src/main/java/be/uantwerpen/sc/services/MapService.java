package be.uantwerpen.sc.services;

import be.uantwerpen.rc.models.map.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private Map map;

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

    }

    @PostConstruct
    public void init() {
        logger.info("Loading robot map from backend...");
        map = loadMap();
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

    public void updateMap() {
        logger.info("Updating map");
        map = loadMap();
    }
}
