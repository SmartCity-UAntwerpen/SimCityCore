package be.uantwerpen.sc.models;

/**
 * Created by Thomas on 5/05/2017.
 * Job class
 * TODO used?
 */
public class Job {

    private int jobId;
    private String jobDesciption;

    public Job(String jobDesciption) {
        this.jobId = 0;
        this.jobDesciption = jobDesciption;
    }

    public Job(int jobId, String jobDesciption) {
        this.jobId = jobId;
        this.jobDesciption = jobDesciption;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobDesciption() {
        return jobDesciption;
    }

    public void setJobDesciption(String jobDesciption) {
        this.jobDesciption = jobDesciption;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", jobDesciption='" + jobDesciption + '\'' +
                '}';
    }
}

