package kazusa.reflex;

import kazusa.annotate.AnnotateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * 注解
 * @author kazusa
 * @version 1.0.0
 */
public class ReflexUtil {

	/**
	 * @param o 传入对象
	 * @return 返回该对象所属类的全限定标识符
	 */
	public static String getClassName(Object o) {
		return o.getClass().getName();
	}

	/**
	 * @param o 传入对象
	 * @return class对象
	 */
	public static Class<?> getClass(Object o) {
		return o.getClass();
	}

	/**
	 * @param path 类的全限定标识符
	 * @return class对象
	 * @throws ClassNotFoundException
	 */
	public static Class<?> getClass(String path) throws ClassNotFoundException {
		return Class.forName(path);
	}

	/**
	 * 反射创建对象
	 * @param aClass 该对象的class对象
	 * @param args 有参构造器参数
	 * @return 该class对象的对象
	 * @param <T> 泛型
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> T getObj(Class<?> aClass, Class<?>... args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		// 获取该class对象所有构造器
		for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
			// 无参构造
			if (args == null || args.length == 0) return (T) constructor.newInstance();
			// 该有参构造的形参数组
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			// 判断形参数是否一致
			if (parameterTypes.length != args.length) continue;
			// 判断形参类型顺序是否一致
			for (int i = 0;i < parameterTypes.length;i++) {
				if (parameterTypes[i] != args[i]) break;
				// 有参构造
				if (i + 1 == parameterTypes.length) return (T) constructor.newInstance(args);
			}
		}
		return null;
	}

	/**
	 * class对象获取实现接口对象
	 * @param aClass 类对象
	 * @param interface_ 实现的接口
	 */
	public static Class<?> getInterface(Class<?> aClass,Class<?> interface_) {
		// 获取该class对象实现接口
		for (Class<?> getInterface : aClass.getInterfaces()) {
			if (interface_ == getInterface) return getInterface;
		}
		return null;
	}

	/**
	 * class对象获取持有的注解对象
	 * @param aClass 类对象
	 * @param annotationClass 注解
	 * @param annotations 排除注解集合
	 * @return 注解对象
	 */
	public static Annotation getAnnotation(Class<?> aClass, Class<? extends Annotation> annotationClass, Set<Class< ? extends Annotation>> annotations) {
		return AnnotateUtil.getAnnotation(aClass,annotationClass,annotations);
	}

	/**
	 * 获取该class对象字段
	 * @param aClass class对象
	 * @param fieldName 字段名
	 * @return 字段对象
	 * @throws NoSuchFieldException 不存在该字段名的字段
	 */
	public static Field getField(Class<?> aClass,String fieldName) throws NoSuchFieldException {
		// 获取该class对象所有字段
		for (Field field : aClass.getDeclaredFields()) {
			if (fieldName.equals(field.getName())) {
				field.setAccessible(true);
				return field;
			}
		}
		// 获取该class对象及其继承父类所有公开字段
		for (Field field : aClass.getFields()) {
			if (fieldName.equals(field.getName())) {
				field.setAccessible(true);
				return field;
			}
		}
		throw new NoSuchFieldException(fieldName);
	}

	/**
	 * 获取该class对象字段值
	 * @param aClass class对象
	 * @param fieldName 字段名
	 * @param args 有参构造器参数
	 * @return 字段值
	 * @throws NoSuchFieldException 不存在该字段名的字段
	 */
	public static Object getFieldValue(Class<?> aClass,String fieldName,Class<?>... args) throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return getField(aClass, fieldName).get(getObj(aClass,args));
	}

	/**
	 * class对象获取方法对象
	 * @param aClass class对象
	 * @param method 方法名
	 * @param invoke 方法返回值
	 * @param args 形参列表类型数组
	 * @return 方法名的方法对象
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static Method getMethod(Class<?> aClass, String method, Class<?> invoke, Class<?>... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (invoke == null) throw new NoSuchMethodException("方法具有返回值类型");
		// 获取该class对象及其继承父类所有公开方法名(包含Object)
		for (Method m : aClass.getMethods()) {
			if (method.equals(m.getName())) {
				// 判断返回值是否一致
				if (invoke != m.getReturnType()) continue;
				// 获取该方法形参列表
				Class<?>[] parameterTypes = m.getParameterTypes();
				// 判断形参数是否一致
				if (args == null || parameterTypes.length != args.length) continue;
				// 判断形参类型顺序是否一致
				for (int i = 0; i < parameterTypes.length; i++) {
					if (parameterTypes[i] != args[i]) break;
					if (i + 1 == parameterTypes.length) return m;
				}
			}
		}
		// 私有方法
		if (args == null || args.length == 0) {
			Method declaredMethod = aClass.getDeclaredMethod(method);
			declaredMethod.setAccessible(true);
			return declaredMethod;
		}
		Method declaredMethod = aClass.getDeclaredMethod(method,args);
		declaredMethod.setAccessible(true);
		return declaredMethod;
	}

	/**
	 * class对象获取方法对象返回值
	 * @param aClass class对象
	 * @param method 方法名
	 * @param invoke 方法返回值
	 * @param args 形参列表
	 * @return 方法名的方法对象返回值
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	public static Object methodValue(Class<?> aClass,String method,Class<?> invoke,Map<Class<?>,Object> args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		// 形参列表类型数组
		Class<? >[] argTypes = new Class[args.size()];
		// 形参列表形参值数组
		Object[] argValues = new Class[args.size()];
		int i = 0;
		for (Map.Entry<Class<?>, Object> entry : args.entrySet()) {
			argTypes[i] = entry.getKey();
			argValues[i] = entry.getValue();
			i++;
		}
		// 方法返回值
		return getMethod(aClass,method,invoke,argTypes).invoke(null,argValues);
	}
}