package kazusa.web;

import kazusa.web.http.AsyncHandle;
import kazusa.web.http.http;
import kazusa.web.ip.ProxyHttp;
import kazusa.web.ip.ProxyIP;
import kazusa.web.ip.ProxyIpPool;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import static kazusa.web.ip.ProxyIpPool.updateHttpProxyIP;

/**
 * @author kazusa
 * @version 1.0.0
 */
public class WebUtil {

	private static URL url;

	private static HashMap<String, String> urlMap;

	/**
	 * @param uri 请求地址
	 * @return 该url信息集合
	 * @throws IOException
	 */
	public static Map<String,String> getUrl(String uri) throws IOException {
		url = new URL(uri);
		urlMap = new HashMap<>();
		urlMap.put("传输协议:",url.getProtocol());
		urlMap.put("IP地址:",url.toString());
		urlMap.put("端口:", String.valueOf(url.getDefaultPort()));
		urlMap.put("网址:",url.getAuthority());
		urlMap.put("网址2:",url.getHost());
		urlMap.put("url相对路径:",url.getPath());
		urlMap.put("参数:",url.getQuery());
		urlMap.put("url相对路径与参数:",url.getFile());
		urlMap.put("port is:", String.valueOf(url.getPort()));
		urlMap.put("ref is:",url.getRef());
		return urlMap;
	}

	public static class TcpUtil {

		public static Socket getSocket(int port) throws IOException {
			return getSocket(String.valueOf(InetAddress.getLocalHost()),port);
		}

		public static Socket getSocket(String ip,int port) throws IOException {
			if (port <= 1000) throw new IllegalArgumentException("port值大于1000");
			return new Socket(ip,port);
		}

		/**
		 * @param port 监听端口
		 * @return 监听端口数据封装的socket对象
		 * @throws IOException
		 */
		public static Socket getServerSocket(int port) throws IOException {
			return new ServerSocket(port).accept();
		}
	}

	public static class UdpUtil {

		/**
		 * @param port 端口
		 * @return udp对象
		 * @throws SocketException
		 */
		public static DatagramSocket getDatagramSocket(int port) throws SocketException {
			return new DatagramSocket(port);
		}

		/**
		 * @param bytes 发送数据包
		 * @param ip IP地址
		 * @param port 端口
		 * @return 发送数据的封装数据对象
		 * @throws UnknownHostException
		 */
		public static DatagramPacket getSendDatagramPacket(byte[] bytes,String ip,int port) throws UnknownHostException {
			return new DatagramPacket(bytes, bytes.length,InetAddress.getByName(ip),port);
		}

		/**
		 * @param bytes 接受数据包
		 * @return 接受数据的封装数据对象
		 */
		public static DatagramPacket getReceiveDatagramPacket(byte[] bytes) {
			return new DatagramPacket(bytes, bytes.length);
		}

		/**
		 * 发送消息
		 * @param responseData 数据
		 * @param ip IP地址
		 * @param port 端口
		 * @throws IOException
		 */
		public static void send(byte[] responseData,String ip,int port) throws IOException {
			getDatagramSocket(port).send(getSendDatagramPacket(responseData,ip,port));
		}

		private static DatagramPacket receiveDatagramPacket;

		/**
		 * 监听端口获取数据
		 * @param port 监听端口
		 * @param bytes 接受数据包
		 * @return 数据封装对象
		 * @throws IOException
		 */
		public static DatagramPacket receive(int port,byte[] bytes) throws IOException {
			receiveDatagramPacket = getReceiveDatagramPacket(bytes);
			// 监听端口等待接受(阻塞态)接受后装包
			getDatagramSocket(port).receive(receiveDatagramPacket);
			return receiveDatagramPacket;
		}
	}

	/**
	 * @param asyncHandle 异步请求处理
	 * @param httpResponseType 确定泛型
	 * @return 返回http对象
	 */
	public static <T> http<T> getHttp(AsyncHandle asyncHandle, T httpResponseType) {
		return new http<>(asyncHandle);
	}

	/**
	 * 链接对象
	 */
	private static ProxyIP connection;

	/**
	 * @param proxyHttp 代理http对象
	 * @return 返回代理http对象
	 * @throws Exception
	 */
	public static <T> http<T> getProxyHttp(ProxyHttp<T> proxyHttp) throws Exception {
		connection = proxyHttp.getProxyIpPool().getConnection();
		if (connection == null) return null;
		http<T> http = getHttp(null,proxyHttp.getHttpResponseType());
		// 配置初始代理
		http.getHttpClient().proxy(ProxySelector.of(new InetSocketAddress(connection.getIp(),connection.getPort())));
		return updateHttpProxyIP(connection,proxyHttp);
	}

	/**
	 * @param i 读取IP数
	 * @param type 读取IP类型
	 * @return 代理IP池
	 * @throws IOException
	 */
	public static ProxyIpPool getProxyIpPool(int i, String type) throws IOException {
		return ProxyIpPool.getProxyIpPool(i,type);
	}
}