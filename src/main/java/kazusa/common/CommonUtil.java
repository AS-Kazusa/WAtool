package kazusa.common;

import kazusa.common.codeoptimize.CodeOptimizeUtil;

/**
 * 通用
 * @author kazusa
 * @version 1.0.0
 */
public class CommonUtil {

	/**
	 * 从一串数字中获取指定位的数
	 * @param l 数
	 * @param right 指定位
	 * @return 指定位的值
	 * @since 1.0.2
	 */
	public static long placeValue(long l,int right) {
		int length = String.valueOf(l).length();
		if (right >= length) right = length - 1;
		if (right < 0 || right == 0) return l % 10;
		/*
	        先进行整除操作，将要求的数字移动到个位上，在使用取余操作，取出最后一位上的值
		    10^(n-1)，然后对10取余就是想要的结果
		    xxx 大数对象?
		*/
		return l / (long) Math.pow(10,right) % 10;
	}


	/**
	 * 查询当前线程堆栈信息查看方法调用顺序,依照栈的方式先进后出
	 * @param i 栈帧值
	 * @return 获取指定的栈帧
	 * @since 1.0.2
	 */
	public static StackTraceElement getMethodStackFrame(int i) {
		if (i < 0) i = 0;
		i = i + 2;
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		//StackTraceElement[] clone = stackTrace.clone();
		if (stackTrace.length == 0) return null;
		if (i >= stackTrace.length) i = stackTrace.length - 1;
		return stackTrace[i];
	}

	/**
	 * 万能流关流方法
	 * @param autoCloseables 实现对象
	 */
	public static void close(AutoCloseable... autoCloseables) {
		CodeOptimizeUtil.tryCatch(() -> {
			for (int i = autoCloseables.length - 1; i >= 0;i--) {
				if (autoCloseables[i] == null) continue;
				autoCloseables[i].close();
			}
		});
	}
}
