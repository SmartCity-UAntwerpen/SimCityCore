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

/**
 * This class creates a client for the use of websockets. (It also keeps the program running)
 */
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

        WebSocketClient simpleWebSocketClient = new StandardWebSocketClient(); //Create websocketclient
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(simpleWebSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient); //create client for stomp messages using sockjs
        stompClient.setMessageConverter(new MappingJackson2MessageConverter()); //Add converter for JSON to the client
        String url = this.url; //Url to connect to
        StompSessionHandler sessionHandler = new MyStompSessionHandler(workerID,this.listener); //create session handler
        StompSession session = null;
        try{
            session = stompClient.connect(url, sessionHandler).get();//Connect to server
            WorkerMessage msg = new WorkerMessage(workerID, SimWorkerType.car,status,botAmount); //create connection message
            session.send("/SimCity/worker/Robot", msg); //send the message
            while (!listener.setSession(session)); //give the session to the controller
        }catch (Exception e){
            System.err.println("error occured " + e);

            e.printStackTrace();
        }

        while (!exit){ //when not shutdown
            try {
                Thread.sleep(5000); //keep executing
            }catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }
        session.disconnect();
    }
    //stop the program
    public void stopProgram(){
        exit = true;
    }

}
