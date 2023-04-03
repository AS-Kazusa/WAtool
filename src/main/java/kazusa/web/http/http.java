package kazusa.web.http;

import kazusa.common.codeoptimize.CodeOptimizeUtil;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * @author kazusa
 * @version 1.0.0
 * @see kazusa.web.WebUtil
 */
public class http<T> {

	public http(AsyncHandle asyncHandle) {
		this.asyncHandle = asyncHandle;
	}

	/**
	 * 快速完成http请求
	 * @param uri 链接
	 * @param requestType 请求类型
	 * @param httpResponseType 数据响应类型
	 * @param isAsync 是否开启异步请求
	 * @return 返回HttpResponse对象
	 */
	public HttpResponse<T> httpRequest(String uri, String requestType,T httpResponseType, boolean isAsync) throws URISyntaxException {
		httpRequestFactory(uri,requestType);
		return httpResponse(httpClient.build(), httpRequest.build(), getBodyHandler(httpResponseType), isAsync);
	}

	private HttpClient.Builder httpClient = HttpClient.newBuilder();

	public HttpClient.Builder getHttpClient() {
		return httpClient;
	}

	/**
	 * @return 返回配置后的httpClient对象
	 */
	public http<T> httpClientConfig() {
		httpClient = HttpConfig.getHttpClient();
		return this;
	}

	private HttpRequest.Builder httpRequest = HttpRequest.newBuilder();

	public HttpRequest.Builder getHttpRequest() {
		return httpRequest;
	}

	public http<T> httpRequestConfig() {
		httpRequest = HttpConfig.getHttpRequest();
		return this;
	}

	public static <T> http<T> getConfigHttp(AsyncHandle asyncHandle) {
		http<T> http = new http<>(asyncHandle);
		http.httpClientConfig().httpRequestConfig();
		return http;
	}

	/**
	 * 请求头集合
	 */
	private List<RequestHeader> requestHeaders = new ArrayList<>();

	public void setRequestHeaders(List<RequestHeader> requestHeaders) {
		if (requestHeaders == null) return;
		this.requestHeaders = requestHeaders;
	}

	public http<T> addRequestHeader(RequestHeader requestHeader) {
		requestHeaders.add(requestHeader);
		return this;
	}

	private String[] requestTypes = {"post","delete","put","get,method:请求方式"};

	private HttpRequest.BodyPublisher ofString;

	/**
	 * http请求对象工厂
	 * @param uri 链接
	 * @param requestType 传入要求请求类型
	 * @return 返回指定请求类型对象
	 */
	private void httpRequestFactory(String uri,String requestType) throws URISyntaxException {
		httpRequest.uri(new URI(uri));
		for (RequestHeader requestHeader : requestHeaders) {
			// 添加请求头信息
			httpRequest.setHeader(requestHeader.getKey(),requestHeader.getValue());
		}
		// 转为大写,因jdk要求传入请求方式字符串为大写
		requestType = requestType.toUpperCase(Locale.ROOT);
		/*
			FIXME: 2023/4/3 注释代码部分为http2.0协议支持请求方式,1.1不支持,现做向下兼容处理
			String temp = null;
			// 匹配指定前缀
			if (requestType.startsWith("METHOD:")) {
				// 保存请求方式
				//temp = requestType.substring(7);
				// 截取前缀去除:
				//requestType = requestType.substring(0, 6);
			}
		 */
		// 匹配指定前缀,保存请求方式
		if (requestType.startsWith("METHOD:")) requestType = requestType.substring(7);
		ofString = HttpRequest.BodyPublishers.ofString(uri, Charset.defaultCharset());
		switch (requestType) {
			case "POST":
				httpRequest = httpRequest.POST(ofString);
				break;
			case "DELETE":
				httpRequest = httpRequest.DELETE();
				break;
			case "PUT":
				httpRequest = httpRequest.PUT(ofString);
				break;
			case "GET":
				httpRequest = httpRequest.GET();
				break;
//			case "METHOD":
//				httpRequest = httpRequest.method(temp,ofString);
			default:
				throw new IllegalArgumentException("不支持该类型请求");
		}
	}

	private String[] bodyTypes = {"byte[]","string","InputStream"};

	/**
	 * 配置响应返回类型
	 * @param httpResponseType 指定响应数据类型
	 * @return 响应类型对象
	 */
	private HttpResponse.BodyHandler getBodyHandler(T httpResponseType) {
		if (httpResponseType instanceof byte[]) return HttpResponse.BodyHandlers.ofByteArray();
		if (httpResponseType instanceof String) return HttpResponse.BodyHandlers.ofString();
		if (httpResponseType instanceof InputStream) return HttpResponse.BodyHandlers.ofInputStream();
		throw new IllegalArgumentException("无配置该响应返回类型");
	}

	private CompletableFuture<HttpResponse<T>> httpResponseCompletableFuture;

	private AsyncHandle asyncHandle;

	/**
	 * 请求响应方法
	 * @param httpClient httpClient对象
	 * @param httpRequest http请求对象
	 * @param httpResponseType 响应数据类型对象
	 * @param isAsync 是否开启异步请求
	 * @return HttpResponse对象
	 */
	private HttpResponse<T> httpResponse(HttpClient httpClient,HttpRequest httpRequest,HttpResponse.BodyHandler<T> httpResponseType,boolean isAsync) {
		// 阻塞调用:同步请求
		if (!isAsync) return CodeOptimizeUtil.tryCatch(() -> httpClient.send(httpRequest, httpResponseType));
		// 多路IO复用:异步请求,调用join方法阻塞线程直到获取结果
		httpResponseCompletableFuture = httpClient.sendAsync(httpRequest, httpResponseType);
		HttpResponse<T> HttpResponse = CodeOptimizeUtil.tryCatch(() -> httpResponseCompletableFuture.get());
		if (HttpResponse != null) return HttpResponse;
		// 异步请求中无需使用到响应对象进行的代码处理
		if (asyncHandle != null) asyncHandle.handle();
		// 阻塞返回
		return httpResponseCompletableFuture.join();
	}

	@Override
	public String toString() {
		System.out.println("支持请求方式:" + Arrays.toString(requestTypes));
		System.out.println("支持响应数据类型:" + Arrays.toString(bodyTypes));
		return "";
	}
}