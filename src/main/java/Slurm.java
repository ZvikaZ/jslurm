import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Slurm {

    private final int DelayBetweenStatusChecks = 300;   //ms

    enum Status {
        INIT,
        SUBMITTED,
        PENDING,
        COMPLETED,
        FAILED
    }

    public Status status;

    private String command;
    private int jobId;
    private String outputFileName;

    public static boolean isSlurmInstalled() {
        return Utils.runCommand("which sbatch") != null;
    }


    public Slurm(String command) {
        this.command = command;
        this.status = Status.INIT;
        this.runJob();
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
        while (status != Status.COMPLETED && status != Status.FAILED) {
            try {
                Thread.sleep(DelayBetweenStatusChecks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateStatus();
        }
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

    public String getOutputFIleName() {
        return outputFileName;
    }

    private void updateStatus() {
        String result = Utils.runCommand(String.format("sacct -j %d --noheader --brief --parsable2 --delimiter=,", this.jobId));
        if (result != "") {
            String state = result.split(",")[1];
            if (Objects.equals(state, "COMPLETED"))
                status = Status.COMPLETED;
            else if (Objects.equals(state, "FAILED"))
                status = Status.FAILED;
            else if (Objects.equals(state, "PENDING"))
                status = Status.PENDING;
            else
                System.out.println("Unrecognized status: " + state);
        }
    }
}
