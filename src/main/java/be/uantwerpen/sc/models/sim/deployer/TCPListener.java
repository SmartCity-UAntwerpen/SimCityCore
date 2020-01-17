package be.uantwerpen.sc.models.sim.deployer;

import java.io.IOException;

/**
 * TCP interface
 */
@Deprecated
public interface TCPListener {
    String parseTCP(String message) throws IOException;
}
