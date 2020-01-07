package be.uantwerpen.sc.Messages;
/**
 * Added By Andreas on 16/12/2019
 * !! Needs to be the same as the messages in SCSimCity
 */
public class ServerMessage {
    private long workerID;
    private WorkerJob job;
    private int botID;
    private String arguments;

    public ServerMessage() {
    }

    public ServerMessage(long workerID, WorkerJob job, int botID, String arguments) {
        this.workerID = workerID;
        this.job = job;
        this.botID = botID;
        this.arguments = arguments;
    }

    public long getWorkerID() {
        return workerID;
    }

    public WorkerJob getJob() {
        return job;
    }

    public int getBotID() {
        return botID;
    }

    public String getArguments() {
        return arguments;
    }
}
