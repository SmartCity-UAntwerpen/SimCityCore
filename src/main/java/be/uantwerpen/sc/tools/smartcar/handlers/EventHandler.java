package be.uantwerpen.sc.tools.smartcar.handlers;

import be.uantwerpen.sc.services.sockets.SimSocket;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Thomas on 28/05/2016.
 */
public class EventHandler
{
    private Queue<Object> events;

    public EventHandler()
    {
        this.events = new LinkedBlockingQueue<>();
    }

    public void addEvent(String event)
    {
        this.events.offer(event);
    }

    public void flushEvents()
    {
        this.events.clear();
    }

    public void processEvents(SimSocket socket)
    {
        while(!this.events.isEmpty())
        {
            String event = (String)this.events.poll();

            socket.sendMessage(event + "\r\n");
        }

        //Read socket to verify if its still alive
        socket.getMessage();
    }
}
