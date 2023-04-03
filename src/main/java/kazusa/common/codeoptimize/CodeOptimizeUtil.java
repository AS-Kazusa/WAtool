package kazusa.common.codeoptimize;

import kazusa.common.CommonUtil;
import kazusa.common.codeoptimize.operateinterface.OperateParameter;
import kazusa.common.codeoptimize.operateinterface.OperateParameterReturn;
import kazusa.common.codeoptimize.operateinterface.OperateReturn;
import kazusa.common.codeoptimize.operateinterface.operate;
import kazusa.io.IOUtil;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 代码优化(dog):将if,for,try等块级操作 -> FP面向函数式编程(操作数据类型在FB之前不可有任何引用或赋值,即保证为final常量状态)
 * 编码形式:boolean ? () -> : () -> || boolean ? class||this::method() : class||this::method()
 * 柯里化(嵌套形式):
 * lambda:boolean ? () -> {boolean ? () -> : () ->} : () -> {boolean ? () -> : () ->}
 * method::引用:boolean ? class||this::method(){boolean ? class||this::method()} : class||this::method(){class||this::method()}
 * boolean ? () -> {class||this::method()} : class||this::method(){() ->}
 * 函数组合(封装形式): () -> 柯里化
 * 偏函数(形参 拼接 内置默认值):(i) -> i + 1;
 * 惰性求值(预先定义多个操作，但不立即求值，在需要使用值时才去求值，可以避免不必要的求值，提升性能):
 * int i = class||obj.method(() -> {i + j});
 * 高阶函数(接收一个或多个函数作为输入,输出一个函数):
 * int i = class||obj.method(() -> i,() -> j);
 * 尾递归(递归形参交换??)
 * @author kazusa
 * @version 1.0.0
 */
public class CodeOptimizeUtil {

	/**
	 * 优化连续&&结构
	 * @param is 布尔值
	 * @return 返回多个&&判断结果
	 */
	private static boolean ands(boolean... is) {
		return ands(arrayList(is));
	}

	/**
	 * 优化连续&&结构
	 * @param bs 布尔值集合
	 * @return 返回多个&&判断结果
	 */
	private static boolean ands(List<Boolean> bs) {
		int i = 0;
		int temp;
		while (i < bs.size()) {
			temp = i;
			// 利用三元运算符递增值确定每个判断结果是否为true
			i = (int) ternaryOperator(bs.get(i),i+ 1,i);
			// 判断结果是否为true返回原值,否则返回is的长度 + 1
			i = (int) ternaryOperator((i - temp) > 0, i,bs.size() + 1);
		}
		// 转换为长度比较确定结果
		return (boolean) ternaryOperator(i == bs.size(),true,false);
	}

	/**
	 * 优化||或连续||-&&结构
	 * @param bs 布尔值集合
	 * @return 返回多个||-&&判断结果
	 */
	private static boolean orAnds(List<Boolean> bs) {
		return (boolean) ternaryOperator(bs.get(0),true,false);
	}

	/**
	 * 优化||或连续&&或&&-||或||-&&结构
	 * @param i 标记||位置,若不存在则为0
	 * @param bs 布尔值
	 * @return 返回多个&&-||判断结果
	 */
	private static boolean orAnds(int i,List<Boolean> bs) {
		int j = 0;
		int temp;
		// 累计是否到达||位置
		int k = 0;
		while (j < bs.size()) {
			temp = j;
			// 利用三元运算符递增j值确定每个判断结果是否为true
			j = (int) ternaryOperator(bs.get(j),j+ 1,j);
			// 判断结果是否为true返回原值,否则返回is的长度 + 1
			j = (int) ternaryOperator((j - temp) > 0, j, bs.size() + 1);
			k++;
			// 判断上一个值结果是否为true并且是否到达标记位,满足则跳出,不满足则继续循环判断
			j = (int) ternaryOperator(ands(k == i,j != bs.size() + 1),bs.size(),j);
		}
		return (boolean) ternaryOperator(j == bs.size(),true,false);
	}

	/**
	 * 判断调用二元运算符方法
	 * @param i 标记||位置,若不存在则为0
	 * @param is 布尔值
	 * @return 布尔值
	 * @throws Exception
	 */
	public static boolean isOrAnds(int i,boolean... is) throws Exception {
		return isOrAnds(i,arrayList(is));
	}

	/**
	 * 判断调用二元运算符方法
	 * @param i 标记||位置,若不存在则为0
	 * @param bs 布尔值集合
	 * @return 布尔值
	 * @throws Exception
	 */
	public static boolean isOrAnds(int i,List<Boolean> bs) throws Exception {
		Integer integer = (Integer) ternaryOperatorPlus(i >= 0, () -> i, () -> {
			throw new IllegalArgumentException("||位置为负数");
		});
		return (boolean) ternaryOperatorPlus
				// &&操作
				(integer == 0,() -> ands(bs),
				// boolean ? ||或连续||-&&操作 : ||或连续&&或&&-||或||-&&操作
				() -> ternaryOperatorPlus(integer == 1,() -> orAnds(bs),() -> orAnds(integer,bs)));
	}

	/**
	 * 以方法形式对三元运算符进行封装
	 * @param is 判断结果
	 * @param if_ if结果
	 * @param else_ else结果
	 * @return 返回if-else赋值
	 */
	public static  <T,U> Object ternaryOperator(boolean is,T if_, U else_) {
		return is ? if_ : else_;
	}

	/**
	 * 以方法形式对三元运算符进行封装,可在返回值之前进行一系列操作
	 * @param is 判断结果
	 * @param operateIF if操作结果
	 * @param operatorElse else操作结果
	 * @return 返回if-else赋值
	 */
	public static <T,U> Object ternaryOperatorPlus(boolean is, OperateReturn<T> operateIF, OperateReturn<U> operatorElse) throws Exception {
		return ternaryOperator(is,operateIF.operateReturnImpl(),operatorElse.operateReturnImpl());
	}

	/**
	 * 优化掉if-else代码块方式,使用lambda表达式和方法引用优化
	 * @param i 标记||位置
	 * @param operateIF if
	 * @param operatorElse else
	 * @param is 判断结果
	 * @return 操作返回结果
	 * @throws Exception
	 */
	@Deprecated
	public static <T,U> Object ifElse(int i,OperateReturn<T> operateIF,OperateReturn<U> operatorElse,boolean ... is) throws Exception {
		return ifElse(i,arrayList(is),operateIF,operatorElse);
	}

	/**
	 * 优化掉if-else代码块方式,使用lambda表达式和方法引用优化
	 * @param i 标记||位置
	 * @param bs 布尔值集合
	 * @param operateIF if
	 * @param operatorElse else
	 * @return 操作返回结果
	 * @throws Exception
	 */
	@Deprecated
	public static <T,U> Object ifElse(int i, List<Boolean> bs,OperateReturn<T> operateIF,OperateReturn<U> operatorElse) throws Exception {
		return ternaryOperatorPlus(isOrAnds(i,bs),operateIF,operatorElse);
	}

	private static List<Boolean> arrayList(boolean... is) {
		List<Boolean> bs = new ArrayList<>();
		for (boolean b: is) {
			bs.add(b);
		}
		return bs;
	}

	/**
	 * 消除以i递增结束的嵌套for
	 * @param operateParameter 形参操作实现对象
	 * @param length 循环结束条件
	 * @throws Exception
	 */
	public static void forI(OperateParameterReturn<Integer> operateParameter, int length) throws Exception {
		for (Integer i = 0; i < length;) {i = operateParameter.operateParameterReturnImpl(i);}
	}

	/**
	 * 消除嵌套for
	 * @param operateParameter 操作对象
	 * @param ts 数组
	 * &#064;SafeVarargs 抑制unchecked堆污染警告
	 * @throws Exception
	 */
	@SafeVarargs
	public static <T> void forEach(OperateParameter<T> operateParameter, T... ts) throws Exception {
		forEach(new ArrayList<>(Arrays.asList(ts)),operateParameter);
	}

	/**
	 * 消除嵌套for
	 * @param list 列表集合
	 * @param operateParameter 操作对象
	 * @throws Exception
	 */
	public static <T> void forEach(List<T> list,OperateParameter<T> operateParameter) throws Exception {
		// 将每个元素包装为一个数组
		for (T t : list) {operateParameter.operateParameterImpl(t);}
	}

	/**
	 * 消除嵌套for
	 * @param set set集合
	 * @param operateParameter 操作对象
	 * @throws Exception
	 */
	public static <T> void forEach(Set<T> set,OperateParameter<T> operateParameter) throws Exception {
		forEach(new ArrayList<>(set),operateParameter);
	}

	/**
	 * 优化掉try-catch代码块异常处理方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 */
	public static void tryCatch(operate operation) {
		try {
		    operation.operateImpl();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 优化掉try-catch代码块异常处理方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @return 返回值
	 */
	public static <T> T tryCatch(OperateReturn<T> operateReturn) {
		try {
			return operateReturn.operateReturnImpl();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 * @param closeables IO流关流
	 */
	public static void tryFinallyClose(operate operation,Closeable... closeables) throws Exception {
		try {
			operation.operateImpl();
		} finally {
			IOUtil.close(closeables);
		}
	}

	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @param closeables IO流关流
	 * @return 返回值
	 * @param <T> 返回值类型
	 * @throws Exception
	 */
	public static <T> T tryFinallyClose(OperateReturn<T> operateReturn, Closeable... closeables) throws Exception {
		try {
			return operateReturn.operateReturnImpl();
		} finally {
			IOUtil.close(closeables);
		}
	}

	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @param closeables IO流关流
	 * @return 返回值
	 * @param <T> 返回值类型
	 */
	@Deprecated
	public static <T> T tryFinallyClose_(Supplier<T>  operateReturn, Closeable... closeables) {
		try {
			return operateReturn.get();
		} finally {
			IOUtil.close(closeables);
		}
	}

	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 * @param autoCloseables 关流对象
	 * @throws Exception
	 */
	public static void tryFinallyClose(operate operation, AutoCloseable... autoCloseables) throws Exception {
		try {
			operation.operateImpl();
		} finally {
			CommonUtil.close(autoCloseables);
		}
	}

	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @param autoCloseables 关流
	 * @return 返回值
	 * @param <T> 返回值类型
	 * @throws Exception
	 */
	public static <T> T tryFinallyClose(OperateReturn<T> operateReturn, AutoCloseable... autoCloseables) throws Exception {
		try {
			return operateReturn.operateReturnImpl();
		} finally {
			CommonUtil.close(autoCloseables);
		}
	}

	/**
	 * 优化掉try-catch-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 * @param closeables IO流关流
	 */
	public static void tryCatchFinallyClose(operate operation,Closeable... closeables) {
		tryCatch(() -> tryFinallyClose(operation,closeables));
	}

	/**
	 * 优化掉try-catch-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @param closeables IO流关流
	 * @return 返回值
	 * @param <T> 返回值类型
	 */
	public static <T> T tryCatchFinallyClose(OperateReturn<T> operateReturn, Closeable... closeables) {
		return tryCatch(() -> tryFinallyClose(operateReturn,closeables));
	}

	/**
	 * 优化掉try-catch-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 * @param autoCloseables 关流对象
	 */
	public static void tryCatchFinallyClose(operate operation,AutoCloseable... autoCloseables) {
		tryCatch(() -> tryFinallyClose(operation,autoCloseables));
	}

	/**
	 * 优化掉try-catch-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operateReturn 带返回值操作
	 * @param autoCloseables 关流对象
	 * @return 返回值
	 * @param <T> 返回值类型
	 */
	public static <T> T tryCatchFinallyClose(OperateReturn<T> operateReturn, AutoCloseable... autoCloseables) {
		return tryCatch(() -> tryFinallyClose(operateReturn,autoCloseables));
	}
}