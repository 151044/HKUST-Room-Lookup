package com.s151044.discord.ratelimit;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executor which imposes a minimum time limit before the next run of the task.
 */
public class LimitedExecutor {
    private long delay;
    private TimeUnit timeUnit;
    private Runnable task;
    private ScheduledExecutorService exec;
    private AtomicBoolean canExec = new AtomicBoolean(true);
    private ScheduledFuture<?> schedule;

    /**
     * Creates a new LimitedExecutor.
     * @param delay Minimum time between each task.
     * @param timeUnit The time unit of {@link LimitedExecutor#LimitedExecutor(long, TimeUnit, Runnable)}  delay}.
     * @param task The task to run each time a submit request is made.
     */
    public LimitedExecutor(long delay, TimeUnit timeUnit, Runnable task){
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.task = task;
        this.exec = Executors.newScheduledThreadPool(2);
    }

    /**
     * Attempts to queue a task for execution. If the delay between each task is not done yet, return false.
     * @return True if execution succeeds, false otherwise
     */
    public boolean queueExecution(){
        if(canExec.get()){
            exec.execute(task);
            canExec.set(false);
            schedule = exec.schedule(() -> canExec.set(true), delay, timeUnit);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the delay before the next invocation of {@link LimitedExecutor#queueExecution()} succeeds.
     * @param unit The unit to retrieve the delay in
     * @return The delay remaining; < 0 when the next invocation is ready.
     */
    public long getDelay(TimeUnit unit){
        if(schedule == null){
            return 0;
        }
        return schedule.getDelay(unit);
    }
}
