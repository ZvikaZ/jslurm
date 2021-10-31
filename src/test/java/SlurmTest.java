import org.junit.jupiter.api.BeforeEach;
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
    void outputFileName() {
        Slurm slurm = new Slurm("pwd");
        slurm.waitFinished();
        String result = Utils.readFileToString(slurm.getOutputFIleName());
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