package br.net.fabiozumbi12.RedProtect.Bukkit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import br.net.fabiozumbi12.RedProtect.RedProtect;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Facilitates Control Flow for the Bukkit Scheduler to easily jump between
 * Async and Sync tasks without deeply nested callbacks, passing the response of the
 * previous task to the next task to use.
 *
 * Usage example: TaskChain.newChain()
 *  .add(new TaskChain.AsyncTask {})
 *  .add(new TaskChain.Task {})
 *  .add(new AsyncTask {})
 *  .execute();
 */
@SuppressWarnings("rawtypes")
public class TaskChain {
    /**
     * Utility helpers for Task returns. Changes the behavior of the Chain when these are returned.
     */
    // Tells a task it will perform call back later.
    public static final Object ASYNC = new Object();
    // Abort executing the chain
    public static final Object ABORT = new Object();


    /**
     * =============================================================================================
     */
	ConcurrentLinkedQueue<BaseTask> chainQueue = new ConcurrentLinkedQueue<BaseTask>();

    boolean executed = false;
    Object previous = null;
    boolean async;
    private final Plugin plugin;
    public TaskChain() {
        this.plugin = RedProtect.plugin; // TODO: Change to get an instance to your plugin!
        this.async = !Bukkit.isPrimaryThread();
    }
    /**
     * =============================================================================================
     */

    /**
     * Starts a new chain.
     * @return
     */
    public static TaskChain newChain() {
        return new TaskChain();
    }

    /**
     * Adds a delay to the chain execution
     * @param ticks # of ticks to delay before next task (20 = 1 second)
     * @return
     */
    public TaskChain delay(final int ticks) {
        add(new GenericTask() {
            {
                // Prevent switching between sync/async
                final BaseTask peek = TaskChain.this.chainQueue.peek();
                this.async = peek != null ? peek.async : TaskChain.this.async;
            }
            @Override
            public void run() {
                final GenericTask task = this;
                task.chain.async = false;
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        task.next();
                    }
                }, ticks);
                async();
            }
        });
        return this;
    }

    /**
     * Adds a step to the chain execution. Async*Task will run off of main thread,
     * *Task will run sync with main thread
     * @param task
     * @return
     */
    public TaskChain add(BaseTask task) {
        synchronized (this) {
            if (executed) {
                throw new RuntimeException("TaskChain is executing");
            }
        }

        chainQueue.add(task);
        return this;
    }

    /**
     * Finished adding tasks, begins executing them.
     */
    public void execute() {
        synchronized (this) {
            if (executed) {
                throw new RuntimeException("Already executed");
            }
            executed = true;
        }
        nextTask();
    }

    /**
     * Fires off the next task, and switches between Async/Sync as necessary.
     */
    private void nextTask() {
        final TaskChain chain = this;
        final BaseTask task = chainQueue.poll();
        if (task == null) {
            // done!
            return;
        }
        if (task.async) {
            if (async) {
                task.run(this);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        chain.async = true;
                        task.run(chain);
                    }
                });
            }
        } else {
            if (async) {
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        chain.async = false;
                        task.run(chain);
                    }
                });
            } else {
                task.run(this);
            }
        }

    }

    /**
     * Provides foundation of a task with what the previous task type should return
     * to pass to this and what this task will return.
     * @param <R> Return Type
     * @param <A> Argument Type Expected
     */
    private abstract static class BaseTask<R, A> {
        TaskChain chain = null;
        boolean async = false;
        boolean executed = false;

        /**
         * Task Type classes will implement this
         * @param arg
         * @return
         */
        protected abstract R runTask(A arg);

        /**
         * Called internally by Task Chain to facilitate executing the task and then the next task.
         * @param chain
         */
        private void run(TaskChain chain) {
            final Object arg = chain.previous;
            chain.previous = null;
            this.chain = chain;
            @SuppressWarnings("unchecked")
			R ret = this.runTask((A) arg);
            if (chain.previous == null) {
                chain.previous = ret;
            }
            if (chain.previous != ASYNC && chain.previous != ABORT) {
                synchronized (this) {
                    executed = true;
                }
                chain.nextTask();
            }
        }

        /**
         * Tells the TaskChain to abort processing any more tasks.
         */
        public R abort() {
            chain.previous = ABORT;
            return null;
        }

        /**
         * Tells the TaskChain you will manually invoke the next task manually using task.next(response);
         */
        public R async() {
            chain.previous = ASYNC;
            return null;
        }

        /**
         * Only to be used when paired with return this.async(); Must be called to execute the next task.
         *
         * To be used inside a callback of another operation that is performed async.
         * @param resp
         */
        public void next(R resp) {
            synchronized (this) {
                if (executed) {
                    throw new RuntimeException(
                        "This task has already been executed. return this.async()");
                }
            }
            chain.async = !Bukkit.isPrimaryThread(); // We don't know where the task called this from.
            chain.previous = resp;
            chain.nextTask();
        }
    }

    /**
     * General abstract classes to be used for various tasks in the chain.
     *
     * First Tasks are for when you do not have or do not care about the return 
     * value of a previous task.
     *
     * Last Tasks are for when you do not need to use a return type.
     *
     * A Generic task simply does not care about Previous Return or return 
     * anything itself.
     *
     * Async Tasks will not run on the Minecraft Thread and should not use the 
     * Bukkit API unless it is thread safe.
     */
    public abstract static class Task<R, A> extends BaseTask<R, A> {
        protected abstract R run(A arg);

        @Override
        protected R runTask(A arg) {
            return run(arg);
        }
    }
    public abstract static class GenericTask extends BaseTask<Object, Object> {
        protected abstract void run();
        @Override
        protected Object runTask(Object arg) {
            run();
            return null;
        }

        public void next() {
            next(null);
        }
    }
    public abstract static class FirstTask<R> extends BaseTask<R, Object> {
        protected abstract R run();

        @Override
        protected R runTask(Object arg) {
            return run();
        }
    }
    public abstract static class LastTask<A> extends BaseTask<Object, A> {
        protected abstract void run(A arg);

        @Override
        protected Object runTask(A arg) {
            run(arg);
            return null;
        }
        public void next() {
            next(null);
        }
    }

    // Async helpers
    public abstract static class AsyncTask<R, A> extends Task<R, A> {{async = true;}}
    public abstract static class AsyncGenericTask extends GenericTask {{async = true;}}
    public abstract static class AsyncFirstTask<R> extends FirstTask<R> {{async = true;}}
    public abstract static class AsyncLastTask<A> extends LastTask<A> {{async = true;}}
}