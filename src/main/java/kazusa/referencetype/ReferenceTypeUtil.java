package kazusa.referencetype;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

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

	HashMap<String, String > myMap  = new HashMap<>(){{
		put("a","b");
		put("b","b");
	}};

	public static void main(String[] args) {
		// 不可变集合
		Map<String, Integer> map = Map.of("Hello", 1, "World", 2);
		// 最后一种好像最多只能存5个
		Map<String, Integer> myMap = ImmutableMap.of("a", 1, "b", 2, "c", 3);
	}


}