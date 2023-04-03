package kazusa.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kazusa
 * @version 1.0.0
 */
public class StringUtil {

	/**
	 * 字符串判空,判断字符串为null || 空字符串 || 空格字符串
	 * @param s 传入判断字符串
	 * @return 判断结果
	 */
	public static boolean isNull(String s) {
		return s == null || s.isBlank();
	}

	/**
	 * 字符串大小写转换,采用字母的ascii值偏移(效果最高)
	 * @param s 字符串
	 * @param model 模式:false为小写转大写,true为大写转小写
	 * @param index 首位
	 * @param end 末位
	 * @return 处理后字符串
	 * @since 1.0.2
	 */
	public static String case_(String s,boolean model,int index,int end) {
		if (index == 0 && end == s.length()) {
			if (model) return s.toLowerCase(Locale.ROOT);
			return s.toUpperCase(Locale.ROOT);
		}
		if (index < 0) {index = 0;} else if (index > s.length()) {index = s.length();}
		if (end < 0) {end = 0;} else if (end > s.length()) {end = s.length();}
		if (index == end) return s;

		char[] chars = s.toCharArray();
		if (model) {
			// 大写转小写
			for (int i = index; i < end; i++) {
				if (chars[i] >= 65 && chars[i] <= 90) chars[i] = (char) (chars[i] + 32);
			}
			return String.valueOf(chars);
		}
		// 小写转大写
		for (int i = index; i < end; i++) {
			// 异或运算
			if (chars[i] >= 97 && chars[i] <= 122) chars[i] ^= 32;
		}
		return String.valueOf(chars);
	}

	/**
	 * 字符串正则整体匹配
	 * @param s 字符串
	 * @param RE 正则表达式||字符串
	 * @return 布尔值
	 */
	public static boolean isStringMatcher(String s,String RE) {
		return Pattern.matches(s,RE);// => s.matches(RE)
	}

	/**
	 * 正则表达式对象
	 */
	private static Pattern pattern;

	/**
	 * 匹配器
	 */
	private static Matcher matcher;

	/**
	 * @param s 字符串
	 * @param RE 正则表达式||字符串
	 * @param is 区分大小写
	 * @return 匹配器
	 */
	public static Matcher getMatcher(String s, String RE,boolean is) {
		// 获取匹配条件,不区分大小写
		if (is) pattern = Pattern.compile(RE,Pattern.CASE_INSENSITIVE);
		pattern = Pattern.compile(RE);
		// 将匹配字符传入匹配器
		return pattern.matcher(s);
	}

	/**
	 * 字符串正则匹配
	 * @param s 字符串
	 * @param RE 非分组或不包含()正则表达式||字符串
	 * @param is 区分大小写
	 * @return 匹配字符集合
	 */
	public static List<MatcherString> stringCanonicalMatcher(String s, String RE,boolean is) {
		List<MatcherString> matcherStrings = new ArrayList<>();
		// 校验正则表达式为非分组
		if (RE.contains("(") && RE.lastIndexOf(")") != -1) return null;
		matcher = getMatcher(s, RE, is);
		while (matcher.find()) {
			matcherStrings.add(new MatcherString(matcher.start(), matcher.group(),matcher.end()));
		}
		return matcherStrings;
	}

	public static class MatcherString {

		private int index;

		private String s;

		private int end;

		public MatcherString(int index, String s, int end) {
			this.index = index;
			this.s = s;
			this.end = end;
		}

		public int getIndex() {
			return index;
		}

		public String getS() {
			return s;
		}

		public int getEnd() {
			return end;
		}

		@Override
		public String toString() {
			return "匹配字符串起始位置:" + index + "\t匹配字符串:" + s + "\t匹配字符串结尾位置 + 1" + end;
		}
	}
}