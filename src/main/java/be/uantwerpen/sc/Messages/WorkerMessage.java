package be.uantwerpen.sc.Messages;

/**
 * Added By Andreas on 16/12/2019
 * !! Needs to be the same as the messages in SCSimCity
 * This class defines the body of the STOMP Messages
 */
public class WorkerMessage {

    private long workerID;
    private SimWorkerType workerType;
    private int status;
    private int botamount;

    public WorkerMessage() {
    }

    public WorkerMessage(long workerID, SimWorkerType workerType, int status, int botamount) {
        this.workerID = workerID;
        this.workerType = workerType;
        this.status = status;
        this.botamount = botamount;
    }

    public long getWorkerID() {
        return workerID;
    }

    public SimWorkerType getWorkerType() {
        return workerType;
    }

    public int getStatus() {
        return status;
    }

    public int getBotamount() {
        return botamount;
    }
}
