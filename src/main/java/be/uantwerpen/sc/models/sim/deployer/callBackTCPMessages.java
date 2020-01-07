package be.uantwerpen.sc.models.sim.deployer;
import java.io.IOException;

/**
 * Created by Andres on 12/12/2019
 * This interface implements the send functionality of the TCPClient
 */
public interface callBackTCPMessages {
    String sendMessage(String message) throws IOException;
    Boolean executeMessage(String message);
}
