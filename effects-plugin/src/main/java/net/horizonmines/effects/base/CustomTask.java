package net.horizonmines.effects.base;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public abstract class CustomTask implements Runnable {

    private final Plugin plugin;
    private final long delay, period;
    private final boolean repeat;
    private final boolean async;

    private BukkitTask task;

    public CustomTask(final @NotNull Plugin plugin,
                      final long delay,
                      final long period,
                      final boolean repeat,
                      final boolean async) {
        this.plugin = plugin;
        this.delay = delay;
        this.period = period;
        this.repeat = repeat;
        this.async = async;
    }

    public void cancel() {
        if (task != null) {
            plugin.getServer().getScheduler().cancelTask(task.getTaskId());
        }
    }

    public void start() {
        // Ensure there is no lingering task before starting a new one.
        cancel();

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (async) {
            if (repeat) {
                this.task = scheduler.runTaskTimerAsynchronously(plugin, this, delay, period);
            } else if (delay > 0) {
                this.task = scheduler.runTaskLaterAsynchronously(plugin, this, delay);
            } else {
                this.task = scheduler.runTaskAsynchronously(plugin, this);
            }
        } else {
            if (repeat) {
                this.task = scheduler.runTaskTimer(plugin, this, delay, period);
            } else if (delay > 0) {
                this.task = scheduler.runTaskLater(plugin, this, delay);
            } else {
                this.task = scheduler.runTask(plugin, this);
            }
        }
    }

    public BukkitTask getTask() {
        return task;
    }
}