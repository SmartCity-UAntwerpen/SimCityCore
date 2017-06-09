package be.uantwerpen.sc.models.sim.deployer;

import java.io.IOException;

public interface TCPListener {
    String parseTCP(String message) throws IOException;
}
