package com.ss.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池的工具类
 */
@Slf4j
public final class ThreadPoolFactoryUtil {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix
     * value: threadPool
     */

    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>(); //实现线程安全

    private ThreadPoolFactoryUtil() {
    }


    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 当不存在要找的线程池时创建，如果找到被停止重新创建并且加入THREAD_POOLS
     *
     * @param customThreadPoolConfig 线程池的基础信息
     * @param threadNamePrefix       线程名称
     * @param deamon
     * @return
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean deamon) {
        //如果 key 对应的 value 不存在，则使用获取 remappingFunction 重新计算后的值，并保存为该 key 的 value，否则返回 value。
        ExecutorService threadpool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, deamon));
        //如果线程池被停止或者完成任务后被停止
        //如果线程池被停止或者完成任务后被停止如果线程池被停止或者完成任务后被停止shutdown()，它可以安全地关闭一个线程池，调用 shutdown() 方法之后线程池并不是立刻就被关闭
        //isShutDown：当调用shutdown()或shutdownNow()方法后返回为true。
        //isTerminated：当调用shutdown()方法后，并且所有提交的任务完成后返回为true;
        //isTerminated：当调用shutdownNow()方法后，成功停止后返回为true;
        //如果线程池任务正常完成，都为false
        if (threadpool.isShutdown() || threadpool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadpool = createThreadPool(customThreadPoolConfig, threadNamePrefix, deamon);
            THREAD_POOLS.put(threadNamePrefix, threadpool);
        }
        return threadpool;
    }

    /**
     * 创建线程池
     *
     * @param customThreadPoolConfig 线程池的初始信息
     * @param threadNamePrefix       作为创建的线程名字的前缀
     * @param deamon                 指定是否为守护进程Deamon Thread
     * @return ExecutorService 异步执行的机制，并且可以让任务在后台执行
     */
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean deamon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, deamon);
        //ThreadPoolExecutor是接口ExecutorService具体实现
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(), customThreadPoolConfig.getKeepAliveTime()
                , customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(), threadFactory);
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param deamon           指定是否为守护进程Deamon Thread
     * @return Thread Factory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean deamon) {
        if (threadNamePrefix != null) {
            if (deamon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(deamon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }


    /**
     * shutdown所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("Call ShutDown all the thread pools method");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminate");
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 打印线程池的信息
     * @param threadPool
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        //线程池定时任务类
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        //定时任务执行
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }

}
