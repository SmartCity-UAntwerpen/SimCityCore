package be.uantwerpen.sc.configurations;

import be.uantwerpen.sc.services.SimDeployerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Thomas on 25/02/2016.
 */
@Configuration
public class SystemLoader implements ApplicationRunner
{
    @Autowired
    SimDeployerService simDeployerService;

    //Run after Spring context initialization
    public void run(ApplicationArguments args)
    {
        new Thread(new StartupProcess()).start();
    }

    private class StartupProcess implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                Thread.sleep(200);
            }
            catch(InterruptedException ex)
            {
                //Thread interrupted
            }

            //Start Simdeployer
            simDeployerService.start();
        }
    }
}
