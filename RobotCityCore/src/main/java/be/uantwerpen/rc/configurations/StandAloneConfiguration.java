package be.uantwerpen.rc.configurations;

import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Created by Thomas on 03/04/2016.
 */
// Configurations for stand-alone mode
@Profile("standalone")
@Configuration
@Import(EmbeddedServletContainerAutoConfiguration.class)
public class StandAloneConfiguration
{
}