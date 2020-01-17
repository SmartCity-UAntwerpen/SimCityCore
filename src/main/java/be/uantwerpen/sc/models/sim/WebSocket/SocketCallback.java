package be.uantwerpen.sc.models.sim.WebSocket;

import be.uantwerpen.sc.Messages.ServerMessage;
import org.springframework.messaging.simp.stomp.StompSession;

/**
 * Interface for the callback on receiving a message
 */
public interface SocketCallback {
    boolean parseMessage(ServerMessage message);
    boolean setSession(StompSession session);
}
