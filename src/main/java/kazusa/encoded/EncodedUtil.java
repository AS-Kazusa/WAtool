package kazusa.encoded;


import cn.hutool.core.codec.Base64;
import kazusa.ce.Digest;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 编码
 * @author kazusa
 * @version 1.0.0
 */
public class EncodedUtil {

	/**
	 * 编码显示
	 * @param bytes hashCode数组
	 * @param digest 数字摘要对象
	 * @return 指定编码格式字符串hashCode
	 */
	public static String encoded(byte[] bytes, Digest digest) {
		switch (digest.getType()) {
			case "Base64":
				return Base64.encode(bytes);
			case "16":
				// 16进制表现
				return new BigInteger(1,bytes).toString(16);
			default:
				return new String(bytes);
		}
	}

	/**
	 * @param s 编码字符串
	 * @return url编码字符串
	 */
	public static String urlEncoded(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	/**
	 *
	 * @param s url编码字符串
	 * @return 还原字符串
	 */
	public static String urlDecoder(String s) {
		return URLDecoder.decode("%E4%B8%AD%E6%96%87%21", StandardCharsets.UTF_8);
	}
}
