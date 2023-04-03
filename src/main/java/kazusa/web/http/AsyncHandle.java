package kazusa.web.http;

/**
 * 异步请求中无需使用到响应对象进行的代码处理
 * @author kazusa
 * @version 1.0.0
 * @see http
 */
public interface AsyncHandle  {

	void handle();
}