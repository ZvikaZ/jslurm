import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlurmTest {
    @Test
    void isSlurmInstalled() {
        assertTrue(Slurm.isSlurmInstalled());
    }

    @Test
    void run() {
        Slurm slurm = new Slurm("ls");
        assertEquals(Slurm.Status.SUBMITTED, slurm.status);
    }

    @Test
    void runEchoHelloWorld() {
        Slurm slurm = new Slurm("echo 'hello world'");
        assertEquals(Slurm.Status.SUBMITTED, slurm.status);
        slurm.waitFinished();
        assertEquals("hello world", slurm.getOutput());
    }

    @Test
    void runMvn() {
        Slurm slurm = new Slurm("mvn exec:java@bpRun -Dexec.args='/cs_storage/zvikah/projects/bpgp/BPGP-wumpus/target/classes/BPGP-6876609382243727218.tmp.js 1'");
        assertEquals(Slurm.Status.SUBMITTED, slurm.status);
    }


    @Test
    void getOutputFileName() {
        Slurm slurm = new Slurm("pwd");
        slurm.waitFinished();
        String result = Utils.readFileToString(slurm.getOutputFileName());
        assertEquals(System.getenv("PWD"), result);
    }

    @Test
    void getOutput() {
        Slurm slurm = new Slurm("pwd");
        slurm.waitFinished();
        String result = slurm.getOutput();
        assertEquals(System.getenv("PWD"), result);
    }

    @Test
    void getStatusOk() throws InterruptedException {
        Slurm slurm = new Slurm("ls");
        slurm.waitFinished();
        assertEquals(Slurm.Status.COMPLETED, slurm.getStatus());
    }

    @Test
    void getStatusFail() throws InterruptedException {
        Slurm slurm = new Slurm("lszzz");
        slurm.waitFinished();
        assertEquals(Slurm.Status.FAILED, slurm.getStatus());
    }

}