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

    private void showHeaders(StompHeaders headers)
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

    private void sendJsonMessage(StompSession session)
    {
         WorkerMessage msg= new WorkerMessage(workerID, SimWorkerType.car,status,botamount);
        session.send("/SimCity/worker/Robot", msg);
    }

    private void subscribeTopic(String topic,StompSession session)
    {
        session.subscribe(topic, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ServerMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers,
                                    Object payload)
            {
                ServerMessage msg = (ServerMessage) payload;
                listener.parseMessage(msg);
                /*if(msg.getJob() == WorkerJob.CONNECTION){
                    session.disconnect();
                }*/
            }
        });
    }

    @Override
    public void afterConnected(StompSession session,
                               StompHeaders connectedHeaders)
    {
        System.err.println("Connected! Headers:");
        showHeaders(connectedHeaders);

        subscribeTopic("/user/queue/worker", session);
        subscribeTopic( "/topic/messages", session);
        subscribeTopic("/topic/shutdown",session);
    }
}
