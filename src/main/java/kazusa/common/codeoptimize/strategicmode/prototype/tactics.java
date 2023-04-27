package kazusa.common.codeoptimize.strategicmode.prototype;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法级策略模式
 * @author kazusa
 * @version 1.0.0
 */
public class tactics<T> {

	/**
	 * 方法对象
	 */
	Method method;

	/**
	 * 调用类
	 */
	Class<?> class_;

	/**
	 * @param class_ 调用类
	 * @param methodName 方法名
	 * @param parameterTypes 方法形参类型
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public tactics(Class<?> class_, String methodName,Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		if (parameterTypes.length == 0) {
			method = Class.forName(class_.getName()).getMethod(methodName);
		} else {
			method = Class.forName(class_.getName()).getMethod(methodName,parameterTypes);
		}
		this.class_ = class_;
	}

	/**
	 * @param args 形参值
	 * @return 获取方法返回值
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Object methodReturn(Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		if (args.length == 0) return method.invoke(Class.forName(class_.getName()).getConstructor().newInstance());
		return method.invoke(Class.forName(class_.getName()).getConstructor().newInstance(),args);
	}
}