package kazusa.annotate;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

/**
 * 注解
 * @author kazusa
 * @version 1.0.0
 */
public class AnnotateUtil {

	/**
	 * 判断该类上是否具有该注解
	 * 该注解的所有扩展注解使用的其他注解必须一致,扩展注解的复合注解一样适用
	 * @param annotatedElement 类
	 * @param annotationClass 注解
	 * @return 布尔值
	 */
	public static boolean isAnnotationInterface(AnnotatedElement annotatedElement, Class< ? extends Annotation> annotationClass) {
		// 注解判断:判断该类上是否存在指定类型的注解
		if (annotatedElement.isAnnotationPresent(annotationClass)) return true;
		// 获取该类上所有注解
		for (Annotation annotation : annotatedElement.getAnnotations()) {
			// 获取注解类
			Class<? extends Annotation> aClass = annotation.annotationType();
			// 过滤
			if (is(aClass)) continue;
			// 递归探查
			return isAnnotationInterface(aClass,annotationClass);
		}
		return false;
	}

	/**
	 * 复合注解排除探查的注解
	 * @param annotationClass 注解类
	 * @return 确定该类是否为排除注解类
	 */
	public static boolean is(Class<? extends Annotation> annotationClass) {
		return annotationClass == Target.class || annotationClass == Retention.class;
	}
}