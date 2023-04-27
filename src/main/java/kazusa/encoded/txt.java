package kazusa.encoded;

import cn.hutool.core.codec.Base64;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * @author kazusa
 * @version 1.1.0
 * @see EncodedUtil
 */
public class txt {

	/**
	 * 文字编码
	 * @param s 字符串
	 * @param algorithm 算法
	 * @return 编码字符串
	 */
	public static String encoded(String s, String algorithm, Charset charset) {
		byte[] bytes = s.getBytes(charset);
		return switch (algorithm) {
			case "base64" -> Base64.encode(bytes);
			case "16" -> new BigInteger(1,bytes).toString(16);
			case "url" -> URLEncoder.encode(s,charset);
			default -> s;
		};
	}

	@Deprecated
	public static String decoder(String s, String algorithm, Charset charset) {
		byte[] bytes = s.getBytes(charset);
		return switch (algorithm) {
			case "base64" -> new String(Base64.decode(bytes));
			case "16" -> new BigInteger(1,bytes).toString(16);
			case "url" -> URLEncoder.encode(s,charset);
			default -> s;
		};
	}
}