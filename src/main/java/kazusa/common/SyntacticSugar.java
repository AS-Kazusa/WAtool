package kazusa.common;

import kazusa.common.operateinterface.OperateParameter;
import kazusa.common.operateinterface.OperateParameterReturn;
import kazusa.common.operateinterface.OperateReturn;
import kazusa.common.operateinterface.operate;
import kazusa.fileio.IOUtil;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 语法糖:将if,for,try等块级操作 -> FP面向函数式编程(操作数据类型在FB之前不可有任何引用或赋值,即保证为final常量状态)
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
 * @version 1.1.0
 * 有没有可能，修改本身就是件错误的事，因为你函数式编程，他是有闭包的，他不一定在这里执行，当你把函数传递到别的地方了，他帮你保存了这个变量，那你这边改了他保存的那份副本又要重新维护，这本身就不合理
 */
public class SyntacticSugar {

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
	public static <T> void forEach(Set<T> set, OperateParameter<T> operateParameter) throws Exception {
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
	 * 优化掉try-catch代码块异常处理方式,使用lambda表达式和方法引用优化
	 * @param t 变量常量化
	 * @param operateReturn 操作
	 * @return 返回值
	 * @param <T> 任意类型
	 */
	@Deprecated
	public static <T> T tryCatch(final T t, OperateParameterReturn<T> operateReturn) {
		try {
			return operateReturn.operateParameterReturnImpl(new AtomicReference<>(t).get());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 优化掉try-finally代码块关流方式,使用lambda表达式和方法引用优化
	 * @param operation 操作
	 * @param closeables IO流关流
	 */
	public static void tryFinallyClose(operate operation, Closeable... closeables) throws Exception {
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
	public static <T> T tryFinallyClose_(Supplier<T> operateReturn, Closeable... closeables) {
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