package kazusa.string;

/**
 * 元字符/正则表达式
 */
public class metacharacterRegularExpression {

	/**
	 * @return 正则表达式
	 */
	public static String regularExpression() {
		return "\\(";
	}

	/**
	 * 元字符
	 */
	private String metacharacter;

	/**
	 * 字符大小写
	 */
	public void text() {
		// 匹配字符默认区分大小写
		metacharacter = "abc";
		// 设置为不区分大小写,作用域为元字符后所有字符
		metacharacter = "(?i)abc";
		// 缩小作用域
		metacharacter = "0((?i)a)";
		// 转义符\\:用于匹配特殊字符并且排除非正则语法
		metacharacter = "\\(";
	}

	/**
	 * 定位符
	 */
	public void location() {
		// 匹配需匹配字符串(整体)首字符为前一个部分,后跟后一个部分
		metacharacter = "^[0-9]+[a-z]";
		// 加强上面一个语法约束,加不加效果相同
		metacharacter = "^[0-9]+[a-z]+$";
		// 匹配条件处于结束位置或空格前一个
		metacharacter = "\\w\\b";
		// 取反,获取字符串非结束位置,空格已经.部分
		metacharacter = "\\w\\B";
	}

	/**
	 * 符号
	 */
	public void symbol() {
		// 表示数值,连续\\d表示连续数
		metacharacter = "\\d\\d";
		// 第二写法
		metacharacter = "\\d{2}";
		// 取反,不匹配数值
		metacharacter = "\\D";
		// 表示大小写英文和数字以及下划线
		metacharacter = "\\w";
		// 取反不匹配
		metacharacter = "\\W";
		// 匹配空格或制表符
		metacharacter = "\\s";
		// 取反
		metacharacter = "\\S";
		// 匹配换行符外所有字符信息
		metacharacter = ".";
		// 匹配字符.
		metacharacter = "\\.";
		// 第二写法
		metacharacter = "[.]";
	}

	/**
	 * 流程控制
	 */
	public void control() {
		// 选择匹配符|:满足其中一个条件即可
		metacharacter = "a|d";
		System.out.println("=========");
		// []:匹配括号内写入任意信息,包括.?等以及特殊字符
		metacharacter = "[abc]";
		// ^取反,不匹配括号内写入任意信息
		metacharacter = "[^abc]";
		// -:匹配A到Z任意字符
		metacharacter = "[A-Z]";
		System.out.println("=========");
		// 限定符,组合前面元字符使用
		// 括号内填入\\d连续作用次数
		metacharacter = "\\d{2}";
		// 范围匹配,最少匹配左,最大匹配右,采用贪婪匹配尽可能匹配大的
		metacharacter = "a{1,3}";
		// 不限制最大匹配数
		metacharacter = "a{1,}";
        /*
            匹配以首字符开头后面符合字符(采取贪婪匹配)
            若元字符后有字符则匹配元字符后第一个字符开头,不匹配首字符
            但元字符前必须有字符
        */
		metacharacter = "ab?";
		// 匹配1到多个,贪心
		metacharacter = "\\d+";
		// 匹配0到多个,贪心
		metacharacter = "\\d*";
		// 将贪心匹配的元字符后加?设置为"非贪心匹配",匹配短的
		metacharacter = "\\d+?";
		grouping();
	}

	/**
	 * 分组
	 */
	public void grouping() {
		// 分组捕获:括号表示分组,一对括号表示一个分组依次类推
		// 非命名捕获
		metacharacter = "(\\d)(\\d)";
		//第二写法
		metacharacter = "((\\d)(\\d))";
        /*
            命名捕获,尖括号内填入命名,为该分组命名
            命名规则:不能包含标点符号且不能数字开头
        */
		metacharacter = "(?<f1>\\d)(?<f2>\\d)";
		// 非捕获分组:括号含义不分组
		// 提取共有部分后跟or匹配
		metacharacter = "a(?:bc|cb)";
		// 匹配括号外字符后跟括号内字符的有多少个
		metacharacter = "a(?=bc|cd)";
		// ?=取反,匹配括号外字符不跟括号内字符有多少个
		metacharacter = "a(?!bc|cd)";
		// 反向引用:分组引用()\\引用分组号,分组外引用()$引用分组号
		// 引用分组,获取连续3个相同数
		metacharacter = "(\\w)\\1{2}";
		// 指定引用分组
		metacharacter = "(\\w)(\\w)\\2";
		metacharacter = "((a)\\1+)$1";
	}
}