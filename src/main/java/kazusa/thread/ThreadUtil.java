package kazusa.thread;

import kazusa.common.codeoptimize.CodeOptimizeUtil;
import kazusa.common.codeoptimize.operateinterface.operate;
import kazusa.string.StringUtil;
import kazusa.thread.threadpool.CustomConfigThreadPool;
import kazusa.thread.threadpool.CustomThreadPool;
import kazusa.thread.threadpool.ThreadPool;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 线程
 * @author kazusa
 * @version 1.0.0
 */
public class ThreadUtil {

	/**
	 * @param runnable 任务
	 * @param i 创建线程数
	 */
	public static void newThreads(Runnable runnable,int i) {
		for (int j = 0; j < i; j++) {
			new Thread(runnable).start();
		}
	}

	private static Thread daemonThread;

	/**
	 * 守护线程
	 * @param operation 传入守护线程实现类
	 * @param threadName 线程名
	 */
	public static void daemonThread(operate operation, String threadName) throws Exception {
		daemonThread = (Thread) CodeOptimizeUtil.ternaryOperatorPlus
		(
			StringUtil.isNull(threadName),
			() -> new Thread(() -> CodeOptimizeUtil.tryCatch(operation)),
			() -> new Thread(() -> CodeOptimizeUtil.tryCatch(operation),threadName)
		);
		daemonThread.setDaemon(true);
		daemonThread.start();
	}

	/**
	 * @param poolType 传入要创建池的类型
	 * @return 返回对应类型Pool的最大资源数
	 */
	public static int getTypePoolSize(String poolType) {
		// cpu密集型应用线程池数:消耗算力
		if (poolType.equalsIgnoreCase("CPU")) return new ThreadPool().getCpuCores() + 1;
		// IO密集型应用线程池数:消耗资源
		return 2 * new ThreadPool().getCpuCores() + 1;
	}

	/**
	 * 线程池工厂
	 * @param name 工厂对应线程池名
	 * @param threadPool 线程池参数对象
	 * @return 返回指定线程池
	 */
	public static ExecutorService ThreadPoolFactory(String name, ThreadPool threadPool) {
		switch (name) {
			case "单线程":
				// 单线程池:以无界队列存放任务,适用于需要保证顺序执行各个任务且在任意时间点不会同时有多个线程
				return Executors.newSingleThreadExecutor();
			case "多线程":
				// 设置常驻线程数并使用无界阻塞队列存放任务,需求大于线程数阻塞等待
				return Executors.newFixedThreadPool(threadPool.getMAX_THREAD_COUNT());
			case "工厂":
				/*
				 设置常驻线程数和线程工厂类,支持定时以及周期性执行任务执行
				 适用于需要多个后台线程执行周期任务的场景
				*/
				return new ScheduledThreadPoolExecutor(threadPool.getMAX_THREAD_COUNT(),threadPool.getThreadFactory());
			case "扩容":
				/*
				  自动扩容缓存线程池:
				  如果这个时候队列满了且正在运行的线程数量小于最大线程数则创建临时线程立刻运行这个任务
				  当一个线程处于空闲状态超过一定的时间(默认1分钟)时判断如果当前运行的线程数大于常驻线程数则停掉该线程
				  自动扩容最大到Integer.MAX_VALUE值(2147483647)
				  适用于服务器负载压力较轻,执行时间较短,任务多的场景
				 */
				return Executors.newCachedThreadPool();
			case "jdk":
				return new CustomConfigThreadPool(threadPool);
			case "自定义":
				return new CustomThreadPool(threadPool);
			case "多队列":
				/*
					多队列并行线程池:配合Fork/Join 框架使用
					适用于大耗时,可并行执行的场景,任务对象需继承ForkJoinTask抽象类
					传入
					并行级别个数:默认为cpu核心数
					并行级别个数对应线程工厂
					处理工作线程未处理异常(默认null)
					用于控制 WorkQueue 的工作模式:队列 -- 反队列
				 */
				return new ForkJoinPool(threadPool.getCpuCores(),ForkJoinPool.defaultForkJoinWorkerThreadFactory,null,true);
			default:
				return null;
		}
	}

	/**
	 * 打印线程池的状态
	 * @param threadPool 线程池
	 * @param time 开始操作延时时间
	 * @param timeout 周期执行时间
	 * @param timeUnit 执行时间单位
	 */
	public static void printThreadPoolStatus(ThreadPoolExecutor threadPool,long time,long timeout,TimeUnit timeUnit) {
		new ScheduledThreadPoolExecutor(1,new ThreadPool().getThreadFactory())
				// 提交定期操作,传入开始操作延时时间,周期执行时间,执行时间单位
				.scheduleAtFixedRate(() -> {
					System.out.println("=========================");
					System.out.println("线程池当前线程数:" + threadPool.getPoolSize());
					System.out.println("正在执行任务的线程数:" +  threadPool.getActiveCount());
					System.out.println("已完成任务数量:" + threadPool.getCompletedTaskCount());
					System.out.println("队列任务数:" + threadPool.getQueue().size());
					System.out.println("=========================");
				}, time,timeout,timeUnit);
	}

	private static final ReentrantLock LOCK = new ReentrantLock();

	/**
	 * @return 返回线程监控对象
	 */
	public static Condition getCondition() {
		return LOCK.newCondition();
	}

	/**
	 * @param runnable 传入实现Runnable任务对象
	 * @return 返回一个无返回值的异步回调对象
	 */
	public CompletableFuture<Void> VoidCompletableFuture(Runnable runnable) {
		return CompletableFuture.runAsync(runnable);
	}

	/**
	 * @param supplier 传入实现Supplier<T>对象
	 * @return 返回一个有返回值的异步回调对象
	 */
	public static <T> CompletableFuture<T> completableFuture(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier);
	}
}
