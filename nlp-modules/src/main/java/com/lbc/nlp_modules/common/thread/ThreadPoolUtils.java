package com.lbc.nlp_modules.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {

	private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);

	private static ExecutorService executor;

	/**
	 * 线程池维护线程的核心(最少)数量
	 */
	private final static int CORE_POOL_SIZE = 20;
	/**
	 * 线程池维护线程的最大数量
	 */
	private final static int MAXIMUM_POOL_SIZE = 20;
	/**
	 * 线程池维护线程所允许的空闲时间
	 */
	private final static int KEEP_ALIVE_TIME = 60;
	/**
	 * 线程池所使用的缓冲队列的最大长度
	 */
	private static final int MaxQueueSize = 500;

	static {
		executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(MaxQueueSize), new NamedThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy());

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				logger.info("shutdown...");
				if ((executor != null) && !executor.isTerminated()) {
					executor.shutdown();
				}
				if ((executor != null) && !executor.isShutdown()) {
					executor.shutdown();
				}
			}
		});
	}

	public static ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * 命名线程工厂类.
	 * 
	 */
	private static class NamedThreadFactory implements ThreadFactory {

		private static AtomicInteger thread_counter = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			final String threadName = "named_thread_" + thread_counter.getAndIncrement();
			thread.setName(threadName);
			thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					logger.error(threadName, e);
				}
			});
			return thread;
		}
	}

	public static int getCorePoolSize() {
		return CORE_POOL_SIZE;
	}

	public static int getMaximumPoolSize() {
		return MAXIMUM_POOL_SIZE;
	}

}
