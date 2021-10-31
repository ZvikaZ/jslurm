import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Slurm {

    private final int DelayBetweenStatusChecks = 300;   //ms

    enum Status {
        INIT,
        SUBMITTED,
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public Status status;
    public String command;
    public int jobId;

    private String outputFileName;

    public static boolean isSlurmInstalled() {
        return Utils.runCommand("which sbatch") != null;
    }


    public Slurm(String command) {
        this.command = command;
        this.status = Status.INIT;
        this.runJob();
    }

    public String toString() {
        return "Slurm. jobID: " + jobId + ", last status: " + status + ". command: " + command;
    }

    private static final Pattern submitPattern = Pattern.compile("Submitted batch job (\\d+)");

    public void runJob() {
        String result = Utils.runCommand(String.format("sbatch --wrap=\"%s\"", this.command));
        if (result == null)
            status = Status.FAILED;
        else {
            Matcher m = submitPattern.matcher(result);
            if (m.find()) {
                status = Status.SUBMITTED;
                jobId = Integer.parseInt(m.group(1));
                updateOutputFileName();
            } else {
                status = Status.FAILED;
            }
        }
    }

    public void waitFinished() {
        updateStatus();
        while (hasntFinished()) {
            try {
                Thread.sleep(DelayBetweenStatusChecks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateStatus();
        }
    }

    private boolean hasntFinished() {
        return status != Status.COMPLETED &&
                status != Status.FAILED &&
                status != Status.CANCELLED;
    }

    private static final Pattern outputPattern = Pattern.compile(".*StdOut=(.+?) .*");

    private void updateOutputFileName() {
        String result = Utils.runCommand(String.format("scontrol show job %d -o", jobId));
        if (result == null)
            status = Status.FAILED;
        else {
            Matcher m = outputPattern.matcher(result);
            if (m.find()) {
                outputFileName = m.group(1);
            } else {
                status = Status.FAILED;
            }
        }
    }


    public Status getStatus() {
        updateStatus();
        return status;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getOutput() {
        return Utils.readFileToString(getOutputFileName());
    }

    private static final Pattern scontrolStatusPattern = Pattern.compile(".*JobState=(.+?) .*");

    private void updateStatus() {
        if (hasntFinished()) {
            // scontrol has disadvantage that it's only for running, or recently finished
            // sacct has disadvantage that it doesn't work on some of our machines
            // .. so, I prefer sacct, and use scontrol as fallback
            String state = "";
            String result = null; //Utils.runCommand(String.format("sacct -j %d --noheader --brief --parsable2 --delimiter=,", this.jobId));
            if (!Objects.equals(result, "") && result != null) {
                state = result.split(",")[1];
            }
            if (result == null) {
                result = Utils.runCommand("scontrol show job " + this.jobId);
                Matcher m = scontrolStatusPattern.matcher(result);
                if (m.find()) {
                    state = m.group(1);
                }
            }

            if (Objects.equals(state, "COMPLETED"))
                status = Status.COMPLETED;
            else if (Objects.equals(state, "FAILED"))
                status = Status.FAILED;
            else if (Objects.equals(state, "PENDING"))
                status = Status.PENDING;
            else if (Objects.equals(state, "RUNNING"))
                status = Status.RUNNING;
            else if (Objects.equals(state, "COMPLETING"))
                // for my needs they are identical
                status = Status.RUNNING;
            else if (Objects.equals(state, "CANCELLED"))
                status = Status.CANCELLED;
            else
                System.out.println("jslurm: Unrecognized status: " + state);
        }
    }
}
