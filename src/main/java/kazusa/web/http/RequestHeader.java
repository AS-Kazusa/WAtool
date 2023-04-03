package kazusa.web.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 请求参数封装对象
 * @author kazusa
 * @version 1.0.0
 * @see http
 */
@AllArgsConstructor
@Getter
@ToString
public class RequestHeader {

	/**
	 * 请求头key
	 */
	private String Key;

	/**
	 * 请求头value
	 */
	private String value;
}