package kazusa.referencetype;

/**
 * 引用类型转换:字符串,数组,集合
 * @author kazusa
 * @version 1.0.0
 */
public class ReferenceTypeUtil {

	/**
	 * 字符串转字符串数组
	 * @param s 字符串
	 * @return 字符串数组
	 */
	public static String[] strArray(String s) {
		char[] chars = s.toCharArray();
		String[] strs = new String[chars.length];
		for (int i = 0; i < chars.length; i++) {
		    strs[i] = String.valueOf(chars[i]);
		}
		return strs;
	}
}