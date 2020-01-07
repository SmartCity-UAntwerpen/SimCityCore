package be.uantwerpen.sc.models.sim.WebSocket;

import be.uantwerpen.sc.Messages.SimWorkerType;
import be.uantwerpen.sc.Messages.WorkerMessage;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerClient extends Thread {
    private String url;
    private long workerID;
    private int status;
    private int botAmount;
    private SocketCallback listener;
    private volatile boolean exit =false;

    public WorkerClient(String url,SocketCallback listener,long workerID, int status, int botAmount){
        this.botAmount = botAmount;
        this.status = status;
        this.url = url;
        this.workerID =workerID;
        this.listener = listener;

    }

    public void run(){

        WebSocketClient simpleWebSocketClient =
                new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(simpleWebSocketClient));

        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient =
                new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String url = this.url;
        StompSessionHandler sessionHandler = new MyStompSessionHandler(workerID,this.listener);
        try{
            StompSession session = stompClient.connect(url, sessionHandler).get();
            WorkerMessage msg = new WorkerMessage(workerID, SimWorkerType.car,status,botAmount);
            session.send("/SimCity/worker/Robot", msg);
            while (!listener.setSession(session)); //give the session to the controller
        }catch (Exception e){
            System.err.println("error occured " + e);

            e.printStackTrace();
        }

        while (!exit){
            try {
                Thread.sleep(5000);
            }catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
    }
    //stop the program
    public void stopProgram(){
        exit = true;
    }

}
