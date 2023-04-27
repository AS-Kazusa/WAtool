package kazusa.common.utils.connectpool;



import kazusa.common.codeoptimize.CodeOptimizeUtil;
import kazusa.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义泛用连接池
 * 默认模板方法适用于资源为整个对象,资源对象封装的请重写资源更新方法updateConnections()和资源回收方法retrieve()
 */
public abstract class CustomConnectPool<T> implements ConnectPool<T> {

	/**
	 * 链接实现对象
	 */
	private final ConnectionImpl<T> CONNECTION_IMPL;

	/**
	 * 链接对象
	 */
	private T connection;

	/**
	 * 核心资源数
	 */
	private final int CORE_NATURAL_RESOURCES;

	/**
	 * 最大资源数
	 */
	private final int MAX_NATURAL_RESOURCES;

	/**
	 * cpu核心数
	 */
	private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

	/**
	 * 链接队列
	 */
	private BlockingQueue<T> connectionQueue;

	/**
	 * 请求队列
	 */
	private BlockingQueue<Object> askQueue;

	/**
	 * 任务队列
	 */
	private BlockingQueue<T> taskQueue;

	/**
	 * 更新资源周期时间
	 */
	private long time = 60;

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
	 * @param connectionImpl 链接实现对象
	 */
	public CustomConnectPool(ConnectionImpl<T> connectionImpl) {
		// IO密集应用:2倍核心数 + 有效磁盘数(数据,被缓存则为0)
		this(connectionImpl,2 * CPU_CORES + 1);
	}

	/**
	 * @param connectionImpl 链接实现对象
	 * @param maxNaturalResources 最大资源数
	 */
	public CustomConnectPool(ConnectionImpl<T> connectionImpl,int maxNaturalResources) {
		this(connectionImpl,maxNaturalResources,maxNaturalResources);
	}

	/**
	 * @param connectionImpl 链接实现对象
	 * @param coreNaturalResources 核心资源数
	 * @param maxNaturalResources 最大资源数
	 */
	public CustomConnectPool(ConnectionImpl<T> connectionImpl,int coreNaturalResources,int maxNaturalResources) {
		this(connectionImpl,coreNaturalResources,maxNaturalResources,maxNaturalResources);
	}

	/**
	 * 配置连接池参数
	 * @param connectionImpl 链接实现对象
	 * @param coreNaturalResources 核心资源数
	 * @param maxNaturalResources 最大资源数
	 * @param queueLength 请求队列长度
	 */
	public CustomConnectPool(ConnectionImpl<T> connectionImpl,int coreNaturalResources, int maxNaturalResources,int queueLength) {
		if (maxNaturalResources == 0) throw new IllegalArgumentException("配置资源数为0,无法初始化连接池");
		CONNECTION_IMPL = connectionImpl;
		CORE_NATURAL_RESOURCES = coreNaturalResources;
		MAX_NATURAL_RESOURCES = maxNaturalResources;
		connectionQueue = new ArrayBlockingQueue<>(coreNaturalResources);
		askQueue = new ArrayBlockingQueue<>(queueLength);
		taskQueue = new ArrayBlockingQueue<>(maxNaturalResources);
	}

	public int getCORE_NATURAL_RESOURCES() {
		return CORE_NATURAL_RESOURCES;
	}

	public int getMAX_NATURAL_RESOURCES() {
		return MAX_NATURAL_RESOURCES;
	}

	/**
	 * 自定义配置队列
	 * @param connectionQueue 链接队列
	 * @param taskQueue 任务队列
	 * @param askQueue 请求队列
	 */
	public void setQueue(BlockingQueue<T> connectionQueue,BlockingQueue<T> taskQueue,BlockingQueue<Object> askQueue) {
		this.connectionQueue = connectionQueue;
		this.taskQueue = taskQueue;
		this.askQueue = askQueue;
	}

	/**
	 * @return 获取当前连接池请求数
	 */
	public int getAskSize() {
	    return askQueue.size();
	}

	/**
	 * @return 获取当前连接池任务数
	 */
	public int getTaskSize() {
		return taskQueue.size();
	}

	public long getTime() {
		return time;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	private AtomicInteger i = new AtomicInteger(0);

	/**
	 * 从链接实现对象中获取链接对象
	 * @param CONNECTION_IMPL 链接实现对象
	 * @return 链接对象
	 * @throws Exception
	 */
	private T getConnection(ConnectionImpl<T> CONNECTION_IMPL) throws Exception {
		// 默认连接池最大资源数(核心资源数)为连接池数据源可提供资源数
		if (i.get() == MAX_NATURAL_RESOURCES) return null;
		// +1
		i.incrementAndGet();
		return CONNECTION_IMPL.getConnection();
	}

	/**
	 * 初始化连接池
	 */
	private void init() throws Exception {
		// 创建核心资源数
		for (int i = 0; i < CORE_NATURAL_RESOURCES; i++) {
			connection = getConnection(CONNECTION_IMPL);
			if (connection == null) throw new IllegalStateException("资源数不足创建对应数量核心资源");
			connectionQueue.put(connection);
		}
		// 创建监控线程更新连接池链接
		monitoringThread();
	}

	/**
	 * 创建非核心资源
	 */
	private T isNewNotCoreThread() throws Exception {
		if (taskQueue.size() + 1 < MAX_NATURAL_RESOURCES) {
			connection = getConnection(CONNECTION_IMPL);
			if (connection == null) return null;
			taskQueue.put(connection);
			return connection;
		}
		return null;
	}

	/**
	 * STW保证返回有效资源
	 */
	private volatile boolean STW;

	private ExecutorService threadPool;

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	/**
	 * 监控线程池核心资源状态
	 */
	private void monitoringThread() throws Exception {
		ThreadUtil.daemonThread(() -> {
			while (true) {
				// 检查时间
				CodeOptimizeUtil.tryCatch(() -> timeUnit.sleep(time));
				STW = true;
				updateConnections(takeNotes());
				STW = false;
			}
		},"监控连接池内核心资源是否有效,无效剔除");
	}

	/**
	 * 记录当前链接队列中资源数
	 * @return 返回记录核心资源集合
	 */
	private List<T> takeNotes() {
		List<T> connections = new ArrayList<>();
		for (int i = 0; i < connectionQueue.size(); i++) {
			connection = connectionQueue.poll();
			connections.add(connection);
			try {
				connectionQueue.put(connection);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return connections;
	}

	/**
	 * 默认采用周期性更新连接池存在所有核心资源
	 * @param connections 记录核心资源集合
	 */
	public List<T> updateConnections(List<T> connections) {
		int size = connections.size();
		// 销毁核心资源
		connections.clear();
		for (int i = 0; i < size; i++) {
			CodeOptimizeUtil.tryCatch(() -> {
				// 创建新的链接放入连接池
				T connection = getConnection(CONNECTION_IMPL);
				if (connection != null) connectionQueue.put(connection);
			});
		}
		return connections;
	}

	/**
	 * 请求链接
	 * @return 返回链接对象
	 */
	private T askConnection() throws InterruptedException {
		// 请求获取链接对象
		askQueue.put(new Object());
		int i = 0;
		while (askQueue.size() > 0) {
			// 默认阻塞1秒 TODO 当前池的设计问题导致无法自定义超时时间
			TimeUnit.SECONDS.sleep(1);
			// 阻塞30秒后仍无法提供资源
			if (connectionQueue.size() == 0 && i == 30) {
				System.err.println("搞笑版:没有那么多资源啦!!!,下次别再取这么多啦,已经一滴都不剩了!");
				// 没有这样的元素异常:定位不到这样的元素,也就是找不到这样的元素。
				throw new NoSuchElementException("正经版:连接池超负荷,无法提供更多资源,可提供最大资源数为:" + this.i.get());
			}
			// 判断是否有核心链接资源
			if (connectionQueue.size() > 0) askQueue.poll();
			i++;
		}
		return connectionQueue.poll();
	}

	/**
	 * 初始化连接池触发值
	 */
	private volatile boolean init = true;

	/**
	 * @return 返回一个链接
	 * @throws InterruptedException
	 */
	public synchronized T getConnection() throws Exception {
		if (init) {
			init();
			init = false;
		}
		// STW时间
		while (STW) {Thread.onSpinWait();}
		// 核心资源
		connection = connectionQueue.poll();
		if (connection != null) {
			taskQueue.put(connection);
			return connection;
		}
		// 非核心资源
		connection = isNewNotCoreThread();
		if (connection != null) return connection;
		// 请求资源
		return askConnection();
	}

	/**
	 * 设置获取核心资源链接对象租期
	 * @param time 租期时间
	 * @param timeUnit 租期时间单位
	 */
	public void setConnectionTenancy(long time,TimeUnit timeUnit) throws Exception {
		ThreadUtil.daemonThread(() -> {
			while (true) {
				CodeOptimizeUtil.tryCatch(() -> timeUnit.sleep(time));
				connection = taskQueue.poll();
				if (connection == null) continue;
				// 强制回收旧资源获取新资源替代
				connection = retrieve(connection);
				if (connection == null) continue;
				connectionQueue.put(connection);
			}
		},"获取链接对象租期");
	}

	/**
	 * 默认回收方法:重置获取新链接
	 * @param connection 需回收链接对象
	 * @return 返回回收链接对象
	 * @throws Exception
	 */
	public T retrieve(T connection) throws Exception {
		connection = null;
		// -1;
		i.decrementAndGet();
		return getConnection(CONNECTION_IMPL);
	}

	/**
	 * @param connection 归还链接
	 */
	public synchronized void setConnection(T connection) {
		taskQueue.poll();
		// 判断所有核心资源数全部归还完毕
		if (connectionQueue.size() == CORE_NATURAL_RESOURCES) {
			connection = null;
			return;
		}
		try {
			connectionQueue.put(connection);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}