package be.uantwerpen.sc.models.sim.WebSocket;

import be.uantwerpen.sc.Messages.ServerMessage;
import be.uantwerpen.sc.Messages.SimWorkerType;
import be.uantwerpen.sc.Messages.WorkerJob;
import be.uantwerpen.sc.Messages.WorkerMessage;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * This class defines how a session with the server is handeled using SOMP
 */
public class MyStompSessionHandler extends StompSessionHandlerAdapter {
    private long workerID;
    private int botamount;
    private int status;
    private SocketCallback listener;
    public MyStompSessionHandler(long workerID,SocketCallback listener)
    {
        this.workerID = workerID;
        this.listener = listener;
    }

    private void showHeaders(StompHeaders headers) //show the headers of a session
    {
        for (Map.Entry<String, List<String>> e:headers.entrySet()) {
            System.err.print("  " + e.getKey() + ": ");
            boolean first = true;
            for (String v : e.getValue()) {
                if ( ! first ) System.err.print(", ");
                System.err.print(v);
                first = false;
            }
            System.err.println();
        }
    }

    private void subscribeTopic(String topic,StompSession session) //subscribe to topics on the server
    {
        session.subscribe(topic, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {  //get the payload of the received messages
                return ServerMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers,       //Handle a message
                                    Object payload)
            {
                ServerMessage msg = (ServerMessage) payload;   //cast the payload to the correct class
                listener.parseMessage(msg);                    //execute the action given in the message
            }
        });
    }

    @Override
    public void afterConnected(StompSession session,        //After a connection is made execute the follwowing commands
                               StompHeaders connectedHeaders)
    {
        System.err.println("Connected! Headers:");
        showHeaders(connectedHeaders);

        subscribeTopic("/user/queue/worker", session);//Subscribe to one on one communication
        subscribeTopic( "/topic/messages", session);  //Actions to be executed
        subscribeTopic("/topic/shutdown",session);    //Shutdown notification
    }
}
