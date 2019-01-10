package be.uantwerpen.sc;

import be.uantwerpen.sc.models.sim.SimCar;
import be.uantwerpen.sc.models.sim.deployer.Log;
import be.uantwerpen.sc.services.SimFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.logging.Level;

// TIP change working dir field to empty in run configuration

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
@SpringApplicationConfiguration(classes = SimCityApplication.class)
//@Ignore
public class SimBotIntegrationTest {

    @Autowired
    SimFactory factory;

    @Test
    public void startSim() throws Exception{
        System.out.println("Starting test...");
        // fix problem ith strange logger implementation
        new Log(this.getClass(), Level.CONFIG);

        SimCar car = factory.createSimCar(1L);
        System.out.println("Car created");
        car.setStartPoint(10);
        Thread.sleep(1000); // wait for application load
        car.start();
        System.out.println("Car started");

        // keep test running
        while(car.isRunning()) {
            Thread.sleep(1000);
        }
    }

}
