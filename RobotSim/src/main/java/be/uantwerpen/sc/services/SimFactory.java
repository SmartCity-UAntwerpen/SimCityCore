package be.uantwerpen.sc.services;

import be.uantwerpen.sc.models.sim.SimCar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimFactory {

    /**
     * Backend IP
     */
    @Value("${robotbackend.ip:default}")
    String robotBackendIP;

    /**
     * Backend Port
     */
    @Value("#{new Integer(${robotbackend.port})}")
    int robotBackendPort;

    public SimCar createSimCar() {
        SimCar car = new SimCar();
        car.setRobotBackendIP(robotBackendIP);
        car.setRobotBackendPort(robotBackendPort);

        return car;
    }
}
