package kazusa.web.ip;

import kazusa.common.codeoptimize.CodeOptimizeUtil;
import kazusa.common.utils.connectpool.ConnectionImpl;
import kazusa.common.utils.connectpool.CustomConnectPool;
import kazusa.fileio.IOUtil;
import kazusa.string.StringUtil;
import kazusa.thread.ThreadUtil;
import kazusa.web.WebUtil;
import kazusa.web.http.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static kazusa.fileio.IOUtil.*;

/**
 * 代理IP池
 * @author kazusa
 * @version 1.0.0
 */
public class ProxyIpPool extends CustomConnectPool<ProxyIP> {

	public ProxyIpPool(ConnectionImpl<ProxyIP> connectionImpl) {
		super(connectionImpl);
	}

	public ProxyIpPool(ConnectionImpl<ProxyIP> connectionImpl, int maxNaturalResources) {
		super(connectionImpl, maxNaturalResources);
	}

	public ProxyIpPool(ConnectionImpl<ProxyIP> connectionImpl, int coreNaturalResources, int maxNaturalResources) {
		super(connectionImpl, coreNaturalResources, maxNaturalResources);
	}

	public ProxyIpPool(ConnectionImpl<ProxyIP> connectionImpl, int coreNaturalResources, int maxNaturalResources, int queueLength) {
		super(connectionImpl, coreNaturalResources, maxNaturalResources, queueLength);
	}

	/**
	 * 配置代理IP池参数
	 * @param ips IP池最大资源数
	 * @param threshold 触发值:使用默认触发值将触发值设为IP池最大资源数
	 * @param time 租期
	 * @param timeUnit 租期单位
	 * @throws Exception
	 */
	public void config(List<ProxyIP> ips,int threshold,long time,TimeUnit timeUnit) throws Exception {
		// 配置代理IP租期
		setConnectionTenancy(time,timeUnit);
		// 配置资源更新触发阈值
		setThreshold(ips,threshold);
		// 默认设定池内IP失效一半后筛选一次
		setTime(getThreshold() * time);
		setTimeUnit(timeUnit);
		// cpu核心数
		int cpuCores = Runtime.getRuntime().availableProcessors();
		setThreadPool(new ScheduledThreadPoolExecutor(2 * cpuCores + 1,Executors.defaultThreadFactory()));
	}

	/**
	 * 链接实现类的资源偏移量
	 */
	private static int i;

	/**
	 * 保存IP类型用于后续向IP池添加IP使用
	 */
	private static String type;

	private static void setType(String type) {
		ProxyIpPool.type = type;
	}

	private static String getType() {
		return type;
	}

	/**
	 * 更新IP池内资源触发值
	 */
	private static int threshold;


	private static int getThreshold() {
		return threshold;
	}

	/**
	 * 设置更新资源触发阈值
	 * @param ips IP池最大资源数
	 * @param threshold 触发值
	 */
	private static void setThreshold(List<ProxyIP> ips,int threshold) {
		if (ips.size() > threshold) ProxyIpPool.threshold = ips.size() - threshold;
	}

	private static List<ProxyIP> ips;

	/**
	 * @param i 读取IP数
	 * @param type 读取IP类型
	 * @return 代理IP池
	 * @throws IOException
	 */
	public static ProxyIpPool getProxyIpPool(int i,String type) throws IOException {
		// 获取IP集合
		ips = getIps(i,type);
		// 保存IP类型用于后续向IP池添加IP使用
		setType(type);
		// 默认触发阈值为IP池内一半IP失效
		setThreshold(ips,ips.size() / 2);
		return new ProxyIpPool(() -> {
			try {
				return ips.get(ProxyIpPool.i++);
			} catch (Exception e) {
				// 异常持久化保存IP
				persistenceIps(ips,zyPath,false,ProxyIpPool.j);
				return null;
			}
		},ips.size());
	}

	private static ProxyIpPool getTestProxyIpPool(List<ProxyIP> ips) {
		return new ProxyIpPool(() -> ips.get(i++),ips.size());
	}

	/**
	 * 测试资源是否有效
	 * @param ips IP集合
	 */
	public static List<ProxyIP> testIP(List<ProxyIP> ips) {
		return getTestProxyIpPool(ips).updateConnections(ips);
	}

	/**
	 * 记录文件存放IP数
	 */
	private static String path = IOUtil.path("/java/src/main/java/javalanguage/utils/web/ip/","README.txt");

	/**
	 * 主用文件
	 */
	private static String zyPath = IOUtil.path("/java/src/main/java/javalanguage/utils/web/ip/","主用.txt");

	/**
	 * 备用文件
	 */
	private static String byPath = IOUtil.path("/java/src/main/java/javalanguage/utils/web/ip/","备用.txt");

	/**
	 * 默认主用文件存放IP数
	 */
	private static int j = 50;

	public static void setI(int i) {
		j = i;
	}

	/**
	 * 追加持久化IP
	 */
	public static void persistenceIps(List<ProxyIP> ips) throws IOException {
		// 筛选可持久化的IP
		ips = testIP(ips);
		// 写入主用文件ip
		boolean b = persistenceIps(ips, zyPath, true, j);
		// 判断主用文件记录IP数是否到达上限
		if (b) persistenceIps(ips,byPath, true, j);
	}

	/**
	 * 更新持久化IP
	 * @param ips IP集合
	 * @throws IOException
	 */
	private static void flushPersistenceIps(List<ProxyIP> ips) throws IOException {
		// 跳过封装方法默认设置
		boolean b = persistenceIps(ips, zyPath, false,ProxyIpPool.i);
		// 判断主用文件记录IP数是否到达上限
		if (b) persistenceIps(ips,byPath, true, i);
	}

	/**
	 * 持久化IP
	 * @param ips IP集合
	 * @param path 存放IP文件路径
	 * @param append 设置输出流是否为追加
	 * @param i 指定文件可存放IP数上限
	 */
	private static boolean persistenceIps(List<ProxyIP> ips,String path,boolean append,int i) throws IOException {
		String zy = "主用文件IP数";
		String by = "备用文件IP数";
		// 累计持久化IP数
		int j = 0;
		// 更新i值:获取已持久化IP数
		if (path.endsWith("主用.txt")) j = getPersistenceIps(zy);
		if (path.endsWith("备用.txt")) j = getPersistenceIps(by);
		try (BufferedWriter bufferedWriter = getWriterOutputStream(path,append)) {
			for (ProxyIP proxyIP : ips) {
				if (j == i && path.endsWith("主用.txt")) {
					isPersistenceIps(zy,j);
					return true;
				}
				bufferedWriter.write(proxyIP.getIp() + ":" + proxyIP.getPort() + ":" + proxyIP.getType());
				// 插入换行符
				bufferedWriter.newLine();
				// 累计新添加IP数
				j++;
			}
		}
		return false;
	}

	/**
	 * 处理字符串数组
	 */
	private static String[] split;

	private static Map<String,String> strings = new HashMap<>();

	/**
	 * 查看记录文件存放IP数
	 * @return 记录文件存放IP总数
	 * @throws IOException
	 */
	public static int getPersistenceIps() throws IOException {
		return getPersistenceIps("主用文件IP数")+ getPersistenceIps("备用文件IP数");
	}

	/**
	 * 查看记录文件存放IP数
	 * @param file 文件
	 * @return 返回文件记录IP数
	 * @throws IOException
	 */
	private static int getPersistenceIps(String file) throws IOException {
		return isPersistenceIps(file,0);
	}

	/**
	 * 记录文件存放IP数
	 * @param file 文件
	 * @param i 新加入IP数
	 * @return 返回文件记录IP数
	 * @throws IOException
	 */
	private static int isPersistenceIps(String file,int i) throws IOException {
		try (BufferedReader bufferedReader = getReaderInputStream(path)) {
			while ((string = bufferedReader.readLine()) != null) {
				split = string.split(":");
				// 判断新加入IP数为0则返回文件记录IP数
				if (i == 0 && split[0].equals(file)) {
					strings.clear();
					return Integer.parseInt(split[1]);
				}
				strings.put(split[0],split[1]);
			}
		}
		try (BufferedWriter bufferedWriter = getWriterOutputStream(path)) {
			boolean is = true;
			for (Map.Entry<String,String> entry: strings.entrySet()) {
				// 不匹配值原路写回
				if (!entry.getKey().equals(file)) {
					bufferedWriter.write(entry.getKey() + ":" + entry.getValue());
					bufferedWriter.newLine();
				}
				// 匹配值更新一次
				if (!is) continue;
				bufferedWriter.write(file + ":" + i);
				bufferedWriter.newLine();
				is = false;
			}
			strings.clear();
		}
		return i;
	}

	/**
	 * 读取IP
	 * @param i 读取IP数
	 * @param type 读取IP类型
	 * @return IP集合
	 * @throws IOException
	 */
	public static List<ProxyIP> getIps(int i,String type) throws IOException {
		// 读取主用文件ip
		List<ProxyIP> ips = getIps(new ArrayList<>(),zyPath,i,type);
		while (true) {
			// 获取IP后筛选一遍保证放到IP池中可用
			ips = testIP(ips);
			if (ips.size() >= i)  break;
			// 读取备用文件ip
			getIps(ips, byPath, i - ips.size(), type);
			// 判断获取IP是否足够,若不足则库存IP不足
			if (ips.size() < 50) {
				System.err.println("库存IP数小于50,请及时补充");
				break;
			}
		}
		flushPersistenceIps(ips);
		return ips;
	}

	/**
	 * 读取IP
	 * @param ips ip集合
	 * @param path 读取路径
	 * @param i 读取IP数
	 * @param type IP类型
	 * @return ip集合
	 * @throws IOException
	 */
	private static List<ProxyIP> getIps(List<ProxyIP> ips,String path,int i,String type) throws IOException {
		int j = 0;
		try (BufferedReader bufferedReader = getReaderInputStream(path)) {
			while ((string = bufferedReader.readLine()) != null) {
				if (j == i) break;
				split = string.split(":");
				// 判断IP类型为空或指定类型则放行
				if (!StringUtil.isNull(type)) if (!type.equals(split[2]))continue;
				ips.add(new ProxyIP(split[0],Integer.parseInt(split[1]),split[2]));
				j++;
			}
		}
		return ips;
	}

	/**
	 * 更新资源后补充足够IP到IP池内
	 * @param ips 更新IP集合
	 * @return 补充IP集合
	 */
	private List<ProxyIP> addIpsPool(List<ProxyIP> ips) {
		return CodeOptimizeUtil.tryCatch(() -> {
			flushPersistenceIps(ips);
			// 跳过封装方法默认设置
			return getIps(ips,zyPath,ips.size() + getThreshold(),getType());
		});
	}

	private ExecutorService threadPool;

	/**
	 * 创建一个计数器对象设置初始值
	 */
	private CountDownLatch countDownLatch;

	/**
	 * 更新核心资源:实现方式:并发测试IP是否有效
	 * @param connections 核心资源集合
	 * @return 更新资源集合
	 */
	@Override
	public List<ProxyIP> updateConnections(List<ProxyIP> connections) {
		threadPool = getThreadPool();
		// 单线程操作
		if (threadPool == null) {
			ips = new ArrayList<>();
			for (ProxyIP proxyIP: connections) {
				runUpdateConnection(proxyIP);
			}
			if (ips.size() <= getThreshold()) ips = addIpsPool(ips);
			return ips;
		}
		// 并发转为线程安全集合操作
		ips = Collections.synchronizedList(new ArrayList<>());
		countDownLatch = new CountDownLatch(connections.size());
		for (ProxyIP proxyIP : connections) {
			threadPool.execute(new RunUpdateConnections(proxyIP));
		}
		// 设置在另一个线程,计数器归零前阻塞该线程
		CodeOptimizeUtil.tryCatch(() -> countDownLatch.await());
		threadPool.shutdown();
		// 触发阈值补充IP
		if (ips.size() <= getThreshold()) ips = addIpsPool(ips);
		return ips;
	}

	private class RunUpdateConnections implements Runnable {

		private ProxyIP proxyIP;

		public RunUpdateConnections(ProxyIP proxyIP) {
			this.proxyIP = proxyIP;
		}

		@Override
		public void run() {
			runUpdateConnection(proxyIP);
			// 设置在线程类上每调用一次减1
			countDownLatch.countDown();
		}
	}

	/**
	 * 可重入锁对象
	 */
	private final ReentrantLock LOCK = new ReentrantLock();

	/**
	 * 更新资源调用方法
	 * @param proxyIP IP对象
	 */
	private void runUpdateConnection(ProxyIP proxyIP) {
		proxyIP = updateConnections1(proxyIP);
		// 判断code码确定方案1可用
		if (proxyIP.getPort() != 200) proxyIP = updateConnections2(proxyIP);
		// 确定若走方案2获得IP是否可用并走方案1获得IP是否可用
		if (proxyIP != null && !StringUtil.isNull(proxyIP.getIp())) ips.add(proxyIP);
	}

	private http<String> http = WebUtil.getHttp(null,"");

	private HttpClient.Builder httpClient;

	private String url = "http://icanhazip.com/";

	private HttpResponse<String> httpResponse;

	/**
	 * 方案1:采用请求网站返回请求IP测试IP是否有效
	 * @param proxyIP 代理IP
	 * @return 代理IP or null
	 */
	private ProxyIP updateConnections1(ProxyIP proxyIP) {
		LOCK.lock();
		try {
			httpClient = http.getHttpClient();
			// 配置代理
			httpClient.proxy(ProxySelector.of(new InetSocketAddress(proxyIP.getIp(),proxyIP.getPort())));
			httpResponse = http.httpRequest(url, "get",false,"");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} finally {
			LOCK.unlock();
		}
		int code = httpResponse.statusCode();
		// 返回代理IP则有效,否则无效
		if (code == 200 && httpResponse.body().equals(proxyIP.getIp())) return proxyIP;
		return new ProxyIP("",code,"");
	}

	private Process exec;

	private BufferedReader bufferedReader;

	private StringBuilder s;

	/**
	 * 方案2:采用ping命令测试IP是否有效
	 * @param proxyIP 代理IP
	 * @return 代理IP or null
	 */
	private ProxyIP updateConnections2(ProxyIP proxyIP) {
		try {
			LOCK.lock();
			// 调用cmd命令检查该IP
			exec = Runtime.getRuntime().exec("ping " + proxyIP.getIp());
			bufferedReader = InputTransformation(exec.getInputStream(), "gbk");
			s = new StringBuilder();
			while ((read = bufferedReader.read(chars)) != -1) {
				s.append(new String(chars, 0, read));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			LOCK.unlock();
			IOUtil.close(bufferedReader);
			// 关闭子进程
			exec.destroy();
		}
		// 获取ping结果查看是否有效
		if (s.lastIndexOf("TTL") != -1) return proxyIP;
		return null;
	}

	/**
	 * 回收核心资源
	 * @param connection 需回收链接对象
	 * @return 返回回收链接对象
	 */
	@Override
	public ProxyIP retrieve(ProxyIP connection) {
		try {
			return new ProxyIP(connection.getIp(),connection.getPort(),"");
		} finally {
			connection = null;
		}
	}

	/**
	 * 更新代理
	 * @param proxyIP 上一个资源对象
	 * @param proxyHttp 代理http对象
	 * @return 返回更新代理后新的代理http对象
	 * @throws Exception
	 */
	public static <T> http<T> updateHttpProxyIP(ProxyIP proxyIP, ProxyHttp<T> proxyHttp) throws Exception {
		AtomicReference<http<T>> tempHttp = new AtomicReference<>();
		ThreadUtil.daemonThread(() -> {
			CodeOptimizeUtil.tryCatch(() -> {
				while (true) {
					// 等待代理失效
					proxyHttp.getTimeUnit().sleep(proxyHttp.getTime());
					if (proxyIP != null) continue;
					ProxyIP connection = proxyHttp.getProxyIpPool().getConnection();
					if (connection == null) {
						tempHttp.set(null);
						return;
					}
					http<T> http = WebUtil.getHttp(null,proxyHttp.getHttpResponseType());
					// 配置代理
					http.getHttpClient().proxy(ProxySelector.of(new InetSocketAddress(connection.getIp(),connection.getPort())));
					tempHttp.set(http);
				}
			});
		},"更新代理");
		return tempHttp.get();
	}
}