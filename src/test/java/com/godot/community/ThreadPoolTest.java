package com.godot.community;

import com.godot.community.service.AlphaService;
import org.junit.Test;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;
import java.awt.*;
import java.util.Date;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {

    private static final Logger looger = LoggerFactory.getLogger(ThreadPoolTest.class);

    // JDK normal threadpool
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    // JDK threadpool for Timer Task
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);


    // Spring normal threadpool
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    // Spring scheduled threadpool
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private AlphaService alphaService;

    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 1. normal JDK threadpoll
    @Test
    public void testExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                looger.debug("Hello ExecutorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        sleep(10000);
    }

    // 2. JDK Timer Task Threadpool
    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                looger.debug("Hello Scheduled Service");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

        sleep(30000);
    }


    // 3. Spring normal threadpool
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                looger.debug("Hello ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; ++i) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }

    // 4. Spring Scheduled threadpool
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                looger.debug("Hello ThreadPoolTaskExecutor");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 10000);

        taskScheduler.scheduleAtFixedRate(task, startTime, 1000);


        sleep(30000);
    }


    // 5. Spring normal threadpool (simplified)
    @Test
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }

        sleep(10000);
    }

    // 6. Spring Scheduled threadpool (simplified)
    @Test
    public void testThreadPoolTaskSchedulerSimpl() {
        sleep(30000);
    }
}
