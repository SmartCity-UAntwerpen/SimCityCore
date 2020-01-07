package be.uantwerpen.sc.models.sim.deployer;

import be.uantwerpen.sc.models.sim.SimCore;
import be.uantwerpen.sc.models.sim.SimStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Andreas on 09/12/2019.
 */
public class TCPClient  extends Thread{

    private SimSocket PollSocket;
    private SimStatus status;
    private SimCore core;
    private String message;
    private String URL;
    private int port;
    private callBackTCPMessages sender;

    public TCPClient(SimCore core, int port, String serverUrl) {
        this.core = core;
        this.URL = serverUrl;
        this.port = port;
        this.status = core.getStatus();
    }

    public void run() {
        while (true){
            try {
                String response = "";
                Boolean finished = false;
                PollSocket = new SimSocket(new Socket(URL, port));
                PollSocket.setTimeOut(500);
                //Send data over the socket
                switch (core.getStatus()){
                    case BOOT:
                        message = sender.sendMessage("BOOTING");
                        response = SendingAndReceive(message);
                        finished = execute(response);
                        if(finished){
                            this.status = SimStatus.RUNNING;
                            PollSocket.close();
                        }else{
                            PollSocket.close();
                            this.status = SimStatus.ERROR;
                        }
                        break;
                    case RUNNING:
                        message = sender.sendMessage("RUNNING");
                        response = SendingAndReceive(message);
                        finished = execute(response);
                        if(finished){
                            this.status = SimStatus.RUNNING;
                            PollSocket.close();
                        }else{
                            PollSocket.close();
                            this.status = SimStatus.ERROR;
                        }
                        break;
                    case SHUTDOWN:
                        message = sender.sendMessage("SHUTDOW");
                        response = SendingAndReceive(message);
                        finished = execute(response);
                        if(finished){
                            this.status = SimStatus.SHUTDOWN;
                            PollSocket.close();
                        }else{
                            PollSocket.close();
                            this.status = SimStatus.ERROR;
                        }
                        break;
                    case ERROR:
                        break;
                }
            }catch (IOException e) {
                this.status = SimStatus.CONNECTION_ERROR;
            }
            core.setStatus(this.status);
        }

    }
    private String SendingAndReceive(String message) throws IOException{
        if(!message.equalsIgnoreCase("UNKNOWN")){
            if (PollSocket.sendMessage(message)) {
                String response = PollSocket.getMessage();
                while (response == null) {
                    response = PollSocket.getMessage();
                }
                return response;
            } else {
                PollSocket.close();
                this.status = SimStatus.CONNECTION_ERROR;
                return "CONNECTION ERROR";
            }
        }else{
            return "INVALID";
        }
    }

    private Boolean execute(String message){
        if(message.equalsIgnoreCase("CONNECTION ERROR") || message.equalsIgnoreCase("UNKNOWN ERROR") ){
            return false;
        }else {
            return sender.executeMessage(message);
        }
    }
}

