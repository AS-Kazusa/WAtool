package kazusa.web.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author kazusa
 * @version 1.0.0
 * @see http
 */
public class HttpConfig {

	/**
	 * @return 返回配置后的httpClient对象
	 */
	protected static HttpClient.Builder getHttpClient() {
		HttpClient.Builder httpClient = HttpClient.newBuilder();
		// 配置证书信息为忽略
		httpClient.sslContext(sslIgnored());
		// 配置使用线程池
		httpClient.executor(Executors.newSingleThreadExecutor());
		// 设置cookie缓存,传入cookie管理器与cookie缓存策略,设置为只接受来自原始服务器的cookie
		httpClient.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
		return httpClient;
	}

	/**
	 * 设置为证书忽略,用于访问使用https协议网站
	 * @return 返回SSLContext对象
	 */
	private static SSLContext sslIgnored() {
		SSLContext sslcontext = null;
		try {
			sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
			// X509ExtendedTrustManager信任管理器:忽略证书验证，忽略主机名检查
			sslcontext.init(null, new TrustManager[] {new X509ExtendedTrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {}
			}}, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
		return sslcontext;
	}

	/**
	 * @return 返回配置后的http请求对象
	 */
	protected static HttpRequest.Builder getHttpRequest() {
		HttpRequest.Builder httpRequest = HttpRequest.newBuilder();
		// 配置使用http版本
		httpRequest.version(HttpClient.Version.HTTP_2);
		return httpRequest;
	}

	/**
	 * 请求头模板
	 * @return 获取默认配置的请求头参数集合
	 */
	public static List<RequestHeader> RequestHeadersFromWork() {
		List<RequestHeader> requestHeaders = new ArrayList<>(4);
		// 设置User-Agent请求头伪装
		requestHeaders.add(new RequestHeader( "User-Agent",""));
		// 设置referer信息
		requestHeaders.add(new RequestHeader("referer",""));
		// 配置Cookie缓存
		requestHeaders.add(new RequestHeader("Cookie",""));
		// 设置返回信息格式
		requestHeaders.add(new RequestHeader("Content-Type","application/json"));
		return requestHeaders;
	}
}