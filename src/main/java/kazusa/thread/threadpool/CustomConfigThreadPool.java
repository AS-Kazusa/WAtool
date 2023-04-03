package kazusa.thread.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 自定义配置线程池
 * @author kazusa
 * @version 1.0.0
 */
public class CustomConfigThreadPool implements ExecutorService {

	/**
	 * 自定义JDK线程池对象
	 */
	private ThreadPoolExecutor threadExecutor;

	/**
	 * cpu核心数
	 */
	private static int cpuCores = Runtime.getRuntime().availableProcessors();

	public CustomConfigThreadPool() {
		this(cpuCores + 1,2 * cpuCores + 1);
	}

	public CustomConfigThreadPool(int maxThreadCount) {
		this(maxThreadCount,maxThreadCount);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 */
	public CustomConfigThreadPool(int coreThreadCount, int maxThreadCount) {
		this(coreThreadCount, maxThreadCount,maxThreadCount);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queueLength 队列长度
	 */
	public CustomConfigThreadPool(int coreThreadCount, int maxThreadCount, int queueLength) {
		this(coreThreadCount, maxThreadCount,new ArrayBlockingQueue<>(queueLength));
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queue 队列
	 */
	public CustomConfigThreadPool(int coreThreadCount, int maxThreadCount, BlockingQueue<Runnable> queue) {
		this(new ThreadPool(coreThreadCount,maxThreadCount,queue));
	}

	private BlockingQueue<Runnable> queue;

	/**
	 * @param threadPool 传入自定义JDK线程池配置参数对象
	 */
	public CustomConfigThreadPool(ThreadPool threadPool) {
		threadExecutor = new ThreadPoolExecutor
						(
							threadPool.getCORE_THREAD_COUNT(),
							threadPool.getMAX_THREAD_COUNT(),
							threadPool.getTime(),
							threadPool.getTimeUnit(),
							threadPool.getQUEUE(),
							threadPool.getThreadFactory(),
							threadPool.getDenialPolicy()
						);
		queue = threadExecutor.getQueue();
	}

	/**
	 * 获取当前运行线程数
	 * @param timeout 每隔多长时间检查一次
	 */
	public void getThreads(long timeout) {
		new Thread(() -> {
			// 调用关闭线程池方法执行完所有已添加所有任务后
			while (!isTerminated() || !isShutdown()) {
				try {
					TimeUnit.SECONDS.sleep(timeout);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.out.println("当前线程池运行线程数:" + threadExecutor.getActiveCount());
			}
		}).start();
	}

	@Override
	public void execute(Runnable command) {
		threadExecutor.execute(command);
	}

	public void shutdown() {
		threadExecutor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return threadExecutor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		// 执行程序关闭返回true
		return threadExecutor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		//如果关闭后所有添加任务已完成则返回true
		return threadExecutor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return threadExecutor.awaitTermination(timeout,unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return threadExecutor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return threadExecutor.submit(task,result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return threadExecutor.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return threadExecutor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return threadExecutor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return threadExecutor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return threadExecutor.invokeAny(tasks, timeout, unit);
	}
}