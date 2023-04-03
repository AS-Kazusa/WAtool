package kazusa.thread.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义线程池
 * @author kazusa
 * @version 1.0.0
 */
public class CustomThreadPool extends AbstractExecutorService {

	/**
	 * 核心线程数
	 */
	private int coreThreadCount;

	/**
	 * 最大线程数
	 */
	private final int MAX_THREAD_COUNT;

	/**
	 * 队列长度
	 */
	private final int QUEUE_LENGTH;

	/**
	 * 任务队列
	 */
	private final BlockingQueue<Runnable> TASK_QUEUE;

	/**
	 * 当前线程数
	 */
	private AtomicInteger nowThreadCount = new AtomicInteger(0);

	/**
	 * 一个线程等待多长时间仍然没有任务就自杀
	 */
	private long time;

	/**
	 * time时间单位
	 */
	private TimeUnit timeUnit;

	/**
	 * 策略选择
	 */
	private int isTactics;

	/**
	 * cpu核心数
	 */
	private static int cpuCores = Runtime.getRuntime().availableProcessors();

	/**
	 * 最佳线程数目 = (线程等待时间与线程CPU时间之比 + 1) * CPU数目
	 * coreThreadCount = (/ + 1) * Runtime.getRuntime().availableProcessors()
	 */
	public CustomThreadPool() {
		this(cpuCores + 1,2 * cpuCores + 1);
	}

	public CustomThreadPool(int MAX_THREAD_COUNT) {
		this(MAX_THREAD_COUNT,MAX_THREAD_COUNT);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 */
	public CustomThreadPool(int coreThreadCount, int maxThreadCount) {
		this(coreThreadCount,maxThreadCount,maxThreadCount);
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queueLength 队列长度
	 */
	public CustomThreadPool(int coreThreadCount, int maxThreadCount, int queueLength) {
		this(coreThreadCount,maxThreadCount,new ArrayBlockingQueue<>(queueLength));
	}

	/**
	 * @param coreThreadCount 核心线程数
	 * @param maxThreadCount 最大线程数
	 * @param queue 队列
	 */
	public CustomThreadPool(int coreThreadCount, int maxThreadCount, BlockingQueue<Runnable> queue) {
		this(new ThreadPool(coreThreadCount,maxThreadCount,queue));
	}

	/**
	 * 配置线程池参数
	 * @param threadPool 传入线程池参数封装对象
	 */
	public CustomThreadPool(ThreadPool threadPool) {
		// 基础参数
		coreThreadCount = threadPool.getCORE_THREAD_COUNT();
		MAX_THREAD_COUNT = threadPool.getMAX_THREAD_COUNT();
		QUEUE_LENGTH = threadPool.getQueueLength();
		TASK_QUEUE = threadPool.getQUEUE();
		// 其他参数
		time = threadPool.getTime();
		timeUnit = threadPool.getTimeUnit();
		isTactics = threadPool.getIsTactics();
	}

	/**
	 * 初始化线程池
	 */
	private void init() {
		// 创建核心线程
		for (int i = 0; i < coreThreadCount; i++) {
			newCoreThread();
		}
	}

	/**
	 * 根据队列中是否有任务创建非核心线程
	 */
	private boolean isNewNotCoreThread(Runnable runnable) {
		if (nowThreadCount.get() <= MAX_THREAD_COUNT && TASK_QUEUE.size() == QUEUE_LENGTH) {
			notCoreThreadRunnable = runnable;
			newNotCoreThread();
			return true;
		}
		return false;
	}

	/**
	 * 拒绝策略
	 * @param runnable 任务
	 * @return 任务
	 * @throws InterruptedException
	 */
	private boolean refuseTactics(Runnable runnable) throws InterruptedException {
		if (nowThreadCount.get() == MAX_THREAD_COUNT && TASK_QUEUE.size() == QUEUE_LENGTH) {
			switch (isTactics) {
				case 0:
					throw new RuntimeException("线程池超载");
				case 1:
					runnable.run();
					return true;
				case 2:
					TASK_QUEUE.poll();
					TASK_QUEUE.put(runnable);
			}
		}
		return true;
	}

	/**
	 * 在未添加任务前启动核心线程空转
	 * @param coreThreadCount 启用核心线程数
	 */
	public void preStartCoreThreads(int coreThreadCount) {
		if (coreThreadCount > this.coreThreadCount) throw new IllegalArgumentException("超过设置核心线程数");
		for (int i = 0; i < coreThreadCount; i++) {
			newCoreThread();
		}
		this.coreThreadCount = this.coreThreadCount - coreThreadCount;
	}

	/**
	 * 初始化线程池触发值和保存强行关闭线程池时任务数
	 */
	private int temp = -2;

	/**
	 * 记录工作核心线程数
	 */
	private AtomicInteger coreTasks = new AtomicInteger(0);

	/**
	 * 添加任务
	 * @param runnable 任务
	 */
	public void execute(Runnable runnable) {
		if (runnable == null) return;
		if (temp == -2) {
			init();
			temp = -1;
		}
		// 调用关闭线程池方法后禁止向线程池添加任务
		if (temp != -1) return;
		// 工作核心线程数 + 1 > 核心线程数将任务放进队列
		if (coreTasks.get() + 1 > coreThreadCount) {
			if (isNewNotCoreThread(runnable)) return;
			try {
				if (refuseTactics(runnable)) return;
				TASK_QUEUE.put(runnable);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		coreThreadRunnable = runnable;
	}

	/**
	 * 执行完所有添加任务后关闭线程池
	 */
	public void shutdown() {
		// 记录关闭线程池时任务数
		temp = TASK_QUEUE.size();
		while (true) {
		    if (nowThreadCount.get() == coreThreadCount && TASK_QUEUE.size() == 0) break;
		}
		shutdownNow();
	}

	/**
	 * 关闭线程池线程触发值
	 */
	private boolean shutdown = true;

	/**
	 * @return 执行shutdown()或shutdownNow()完全终止则返回true
	 */
	public boolean isShutdown() {
		return !shutdown;
	}

	/**
	 * @return 执行shutdown()或shutdownNow()但未完全终止则返回true
	 */
	@Override
	public boolean isTerminated() {
		return shutdown;
	}

	private List<Runnable> list = new ArrayList<>();

	/**
	 * 强行关闭线程池
	 * @return 返回未执行任务集合
	 */
	public List<Runnable> shutdownNow() {
		// 记录关闭线程池时任务数
		temp = TASK_QUEUE.size();
		this.shutdown = false;
		for (int i = 0; i < TASK_QUEUE.size(); i++) {
			list.add(TASK_QUEUE.poll());
		}
		return list;
	}

	/**
	 * 创建核心线程
	 */
	private void newCoreThread() {
		new Thread(this::coreThreadRun, String.valueOf(nowThreadCount.incrementAndGet())).start();
	}

	/**
	 * 创建非核心线程
	 */
	private void newNotCoreThread() {
		new Thread(this::notCoreThreadRun, String.valueOf(nowThreadCount.incrementAndGet())).start();
	}

	/**
	 * 执行任务数
	 */
	private AtomicLong tasks = new AtomicLong(0);

	/**
	 * @return 返回已完成执行任务总数
	 */
	public AtomicLong getTasks() {
		return tasks;
	}

	/**
	 * 核心任务
	 */
	private Runnable coreThreadRunnable;

	/**
	 * 核心线程工作方法
	 */
	private void coreThreadRun() {
		while (shutdown) {
			// 判断核心线程获取任务执行方式
			if (TASK_QUEUE.size() == 0) {
				if (coreThreadRunnable == null) continue;
				// 代表一个核心线程工作
				coreTasks.incrementAndGet();
				try {
					coreThreadRunnable.run();
					coreThreadRunnable = null;
				} catch (Exception e) {
					// 代表一个核心线程工作结束
					coreTasks.decrementAndGet();
					continue;
				}
				// 代表一个核心线程工作结束
				coreTasks.decrementAndGet();
				tasks.incrementAndGet();
				continue;
			}
			coreThreadRunnable = TASK_QUEUE.poll();
			if (coreThreadRunnable == null) continue;
			try {
				coreThreadRunnable.run();
				coreThreadRunnable = null;
			} catch (Exception e) {
				continue;
			}
			tasks.incrementAndGet();
		}
	}

	/**
	 * 非核心任务
	 */
	private Runnable notCoreThreadRunnable;

	/**
	 * 非核心线程工作方法
	 */
	private void notCoreThreadRun() {
		while (!nullThread() || shutdown) {
			if (notCoreThreadRunnable == null) continue;
			try {
				notCoreThreadRunnable.run();
				notCoreThreadRunnable = null;
			} catch (Exception e) {
				continue;
			}
			tasks.incrementAndGet();
		}
	}

	/**
	 * 判断是否要销毁空闲的非核心线程
	 * 锁用来锁住线程销毁,避免销毁的线程超出预期
	 * @return 布尔值
	 */
	 private synchronized boolean nullThread() {
		if (coreThreadRunnable == null && notCoreThreadRunnable == null && nowThreadCount.get() > coreThreadCount) {
			try {
				// 阻塞一段时间后是否无任务再销毁
				awaitTermination(time * 60,timeUnit);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (TASK_QUEUE.size() != 0) return false;
		}
		System.out.println("销毁非核心线程:线程ID:" + Thread.currentThread().getName());
		// 更新当前线程数记录值:-1返回新值
		nowThreadCount.decrementAndGet();
		return true;
	}

	/**
	 * 阻塞线程
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * @return 布尔值
	 * @throws InterruptedException
	 */
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		unit.sleep(timeout);
		return true;
	}
}