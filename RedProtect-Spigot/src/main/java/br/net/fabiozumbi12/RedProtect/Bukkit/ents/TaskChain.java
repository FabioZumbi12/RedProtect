/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 22:14
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.ents;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("rawtypes")
public class TaskChain {

    // Tells a task it will perform call back later.
    public static final Object ASYNC = new Object();
    // Abort executing the chain
    public static final Object ABORT = new Object();


    final ConcurrentLinkedQueue<BaseTask> chainQueue = new ConcurrentLinkedQueue<>();
    private final Plugin plugin;
    boolean executed = false;
    Object previous = null;
    boolean async;

    public TaskChain() {
        this.plugin = RedProtect.get(); // TODO: Change to get an instance to your plugin!
        this.async = !Bukkit.isPrimaryThread();
    }
    /*
      =============================================================================================
     */

    public static TaskChain newChain() {
        return new TaskChain();
    }

    public TaskChain add(BaseTask task) {
        synchronized (this) {
            if (executed) {
                throw new RuntimeException("TaskChain is executing");
            }
        }

        chainQueue.add(task);
        return this;
    }

    public void execute() {
        synchronized (this) {
            if (executed) {
                throw new RuntimeException("Already executed");
            }
            executed = true;
        }
        nextTask();
    }

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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    chain.async = true;
                    task.run(chain);
                });
            }
        } else {
            if (async) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    chain.async = false;
                    task.run(chain);
                });
            } else {
                task.run(this);
            }
        }

    }

    private abstract static class BaseTask<R, A> {
        TaskChain chain = null;
        boolean async = false;
        boolean executed = false;

        protected abstract R runTask(A arg);

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

        public R abort() {
            chain.previous = ABORT;
            return null;
        }

        public void async() {
            chain.previous = ASYNC;
        }

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
    public abstract static class AsyncTask<R, A> extends Task<R, A> {
        {
            async = true;
        }
    }

    public abstract static class AsyncGenericTask extends GenericTask {
        {
            async = true;
        }
    }

    public abstract static class AsyncFirstTask<R> extends FirstTask<R> {
        {
            async = true;
        }
    }

    public abstract static class AsyncLastTask<A> extends LastTask<A> {
        {
            async = true;
        }
    }
}