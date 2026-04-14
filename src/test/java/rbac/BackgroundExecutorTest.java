package rbac;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundExecutorTest {

    @Test
    void tasksRunInBackground() throws Exception {
        RBACSystem sys = new RBACSystem();
        sys.initialize();
        CountDownLatch latch = new CountDownLatch(1);

        sys.getBackgroundExecutor().submit(latch::countDown);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "background task did not run");
        sys.shutdown();
    }
}

