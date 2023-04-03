package kazusa.thread.threadpool;

import lombok.Data;

import java.util.Set;
import java.util.concurrent.*;

/**
 * 自定义线程池避免JDK内置线程池出现的线程数过大和请求任务堆积造成的OOM内齿泄露问题
 * @author kazusa
 * @version 1.0.0
 */
@Data
public class ThreadPool {

	/**
	 * 核心线程数
	 */
	private final int CORE_THREAD_COUNT;

	/**
	 * 最大线程数
	 */
	private final int MAX_THREAD_COUNT;

	/**
	 * 队列长度
	 */
	private int queueLength = getMAX_THREAD_COUNT();

	/**
	 * 队列
	 */
	private final BlockingQueue<Runnable> QUEUE;

	/**
	 * 空闲线程存活时间:一个线程等待多长时间仍然没有任务就自杀
	 */
	private long time = 1L;

	/**
	 * time时间单位:默认秒
	 * 1、NANOSECONDS：纳秒（=千分之一微妙）
	 * 2、MICROSECONDS：微妙（=千分之一毫秒）
	 * 3、MILLISECONDS：毫秒（=千分之一秒）
	 * 4、SECONDS：秒
	 * 5、MINUTES：分钟
	 * 6、HOURS：小时
	 * 7、DAYS：天
	 */
	private TimeUnit timeUnit = TimeUnit.SECONDS;

	/**
	 * 默认线程工厂类
	 */
	private ThreadFactory threadFactory = Executors.defaultThreadFactory();

	/**
	 * 我的线程工厂
	 * https://www.cnblogs.com/itbac/p/12210680.html
	 */
	private static class MyThreadFactory implements ThreadFactory {

		private final ThreadFactory threadFactory;
		private final Set<Thread> threadsContainer;

		private MyThreadFactory(ThreadFactory threadFactory, Set<Thread> threadsContainer) {
			this.threadFactory = threadFactory;
			this.threadsContainer = threadsContainer;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = threadFactory.newThread(r);
			//cache thread 记录线程
			threadsContainer.add(thread);
			//删除不存活的线程
//            threadsContainer.removeIf(next -> !next.isAlive());
			return thread;
		}
	}

	/**
	 * 策略选择:决策 ? 抛出异常 : 阻塞二次判断处理
	 */
	private int isTactics;

	/**
	 * 拒绝策略
	 */
	private RejectedExecutionHandler denialPolicy = new ThreadPoolExecutor.AbortPolicy();

	/**
	 * cpu核心数
	 */
	private static int cpuCores = Runtime.getRuntime().availableProcessors();

	public ThreadPool() {
		// 设置核心线程数为
		this(cpuCores + 1,2 * cpuCores + 1);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 */
	public ThreadPool(int coreThreadCount, int maxThreadCount) {
		this(coreThreadCount,maxThreadCount,maxThreadCount);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queueLength 自定义队列长度
	 */
	public ThreadPool(int coreThreadCount, int maxThreadCount,int queueLength) {
		this(coreThreadCount,maxThreadCount,new ArrayBlockingQueue<>(queueLength));
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queue 队列
	 */
	public ThreadPool(int coreThreadCount, int maxThreadCount,BlockingQueue<Runnable> queue) {
		CORE_THREAD_COUNT = coreThreadCount;
		MAX_THREAD_COUNT = maxThreadCount;
		QUEUE = queue;
	}

	/**
	 * @return cpu核心数
	 */
	public int getCpuCores() {
		return cpuCores;
	}
}