package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.ProfilerMeasured;
import net.optifine.Config;
import net.optifine.util.PacketRunnable;
import org.slf4j.Logger;

public abstract class BlockableEventLoop<R extends Runnable> implements Executor, TaskScheduler<R>, ProfilerMeasured {
    public static final long BLOCK_TIME_NANOS = 100000L;
    private static volatile Supplier<CrashReport> delayedCrash;
    private final boolean propagatesCrashes;
    private final String name;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
    private int blockingCount;

    protected BlockableEventLoop(String name, boolean propagatesCrashes) {
        this.propagatesCrashes = propagatesCrashes;
        this.name = name;
        MetricsRegistry.INSTANCE.add(this);
        if (this instanceof Minecraft mc) {
            Config.initMinecraft(mc);
        }
    }

    protected abstract boolean shouldRun(R var1);

    public boolean isSameThread() {
        return Thread.currentThread() == this.getRunningThread();
    }

    protected abstract Thread getRunningThread();

    protected boolean scheduleExecutables() {
        return !this.isSameThread();
    }

    public int getPendingTasksCount() {
        return this.pendingRunnables.size();
    }

    @Override
    public String name() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> supplier) {
        return this.scheduleExecutables() ? CompletableFuture.supplyAsync(supplier, this) : CompletableFuture.completedFuture(supplier.get());
    }

    private CompletableFuture<Void> submitAsync(Runnable runnable) {
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        }, this);
    }

    @CheckReturnValue
    public CompletableFuture<Void> submit(Runnable runnable) {
        if (this.scheduleExecutables()) {
            return this.submitAsync(runnable);
        }

        runnable.run();
        return CompletableFuture.completedFuture(null);
    }

    public void executeBlocking(Runnable runnable) {
        if (!this.isSameThread()) {
            this.submitAsync(runnable).join();
        } else {
            runnable.run();
        }
    }

    @Override
    public void schedule(R r) {
        this.pendingRunnables.add(r);
        LockSupport.unpark(this.getRunningThread());
    }

    @Override
    public void execute(Runnable command) {
        R task = this.wrapRunnable(command);
        if (this.scheduleExecutables()) {
            this.schedule(task);
        } else {
            this.doRunTask(task);
        }
    }

    public void executeIfPossible(Runnable command) {
        this.execute(command);
    }

    protected void dropAllTasks() {
        this.pendingRunnables.clear();
    }

    protected void runAllTasks() {
        int count = Integer.MAX_VALUE;
        if (Config.isLazyChunkLoading() && this == Minecraft.getInstance()) {
            count = this.getTaskCount();
        }

        while (this.pollTask()) {
            if (--count <= 0) {
                break;
            }
        }
    }

    protected boolean shouldRunAllTasks() {
        return this.blockingCount > 0;
    }

    protected boolean pollTask() {
        this.throwDelayedException();
        R task = this.pendingRunnables.peek();
        if (task == null) {
            return false;
        }

        if (!this.shouldRunAllTasks() && !this.shouldRun(task)) {
            return false;
        }

        this.doRunTask(this.pendingRunnables.remove());
        return true;
    }

    public void managedBlock(BooleanSupplier condition) {
        this.blockingCount++;

        try {
            while (!condition.getAsBoolean()) {
                if (!this.pollTask()) {
                    this.waitForTasks();
                }
            }
        } finally {
            this.blockingCount--;
        }
    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void doRunTask(R task) {
        try (Zone ignored = TracyClient.beginZone("Task", SharedConstants.IS_RUNNING_IN_IDE)) {
            task.run();
        } catch (Exception e) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", this.name(), e);
            if (isNonRecoverable(e)) {
                throw e;
            }
        }
    }

    @Override
    public List<MetricSampler> profiledMetrics() {
        return ImmutableList.of(MetricSampler.create(this.name + "-pending-tasks", MetricCategory.EVENT_LOOPS, this::getPendingTasksCount));
    }

    public static boolean isNonRecoverable(Throwable t) {
        return t instanceof ReportedException r ? isNonRecoverable(r.getCause()) : t instanceof OutOfMemoryError || t instanceof StackOverflowError;
    }

    private void throwDelayedException() {
        if (this.propagatesCrashes) {
            Supplier<CrashReport> delayedCrash = BlockableEventLoop.delayedCrash;
            if (delayedCrash != null) {
                throw new ReportedException(delayedCrash.get());
            }
        }
    }

    public void delayCrash(CrashReport crashReport) {
        delayedCrash = () -> crashReport;
    }

    public static synchronized void relayDelayCrash(CrashReport crashReport) {
        Supplier<CrashReport> delayedCrash = BlockableEventLoop.delayedCrash;
        if (delayedCrash == null) {
            BlockableEventLoop.delayedCrash = () -> crashReport;
        } else {
            delayedCrash.get().getException().addSuppressed(crashReport.getException());
        }
    }

    private int getTaskCount() {
        if (this.pendingRunnables.isEmpty()) {
            return 0;
        }

        R[] rs = (R[])this.pendingRunnables.toArray(new Runnable[this.pendingRunnables.size()]);
        double chunkUpdateWeight = this.getChunkUpdateWeight(rs);
        if (chunkUpdateWeight < 5.0) {
            return Integer.MAX_VALUE;
        }

        int queueSize = rs.length;
        int fps = Math.max(Config.getFpsAverage(), 1);
        double weight = queueSize * 10 / fps;
        return this.getCount(rs, weight);
    }

    private int getCount(R[] rs, double maxWeight) {
        double weight = 0.0;

        for (int i = 0; i < rs.length; i++) {
            R r = rs[i];
            weight += this.getChunkUpdateWeight(r);
            if (weight > maxWeight) {
                return i + 1;
            }
        }

        return rs.length;
    }

    private double getChunkUpdateWeight(R[] rs) {
        double weight = 0.0;

        for (int i = 0; i < rs.length; i++) {
            R r = rs[i];
            weight += this.getChunkUpdateWeight(r);
        }

        return weight;
    }

    private double getChunkUpdateWeight(Runnable r) {
        if (r instanceof PacketRunnable pr) {
            Packet p = pr.getPacket();
            if (p instanceof ClientboundLevelChunkWithLightPacket) {
                return 1.0;
            }

            if (p instanceof ClientboundLightUpdatePacket) {
                return 0.2;
            }

            if (p instanceof ClientboundForgetLevelChunkPacket) {
                return 2.6;
            }
        }

        return 0.0;
    }
}
