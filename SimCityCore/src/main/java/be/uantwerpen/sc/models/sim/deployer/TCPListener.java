package be.uantwerpen.sc.models.sim.deployer;

import java.io.IOException;

/**
 * TCP interface
 */
public interface TCPListener {
    String parseTCP(String message) throws IOException;
}
