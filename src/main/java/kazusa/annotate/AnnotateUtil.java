package kazusa.annotate;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

/**
 * 注解
 * @author kazusa
 * @version 1.0.0
 */
public class AnnotateUtil {

	/**
	 * 默认排除的注解
	 * @return 排除注解集合
	 */
	public static Set<Class< ? extends Annotation>> getIsAnnotations() {
		Set<Class< ? extends Annotation>> annotations = new HashSet<>();
		annotations.add(Target.class);
		annotations.add(Retention.class);
		return annotations;
	}

	/**
	 * 过滤:判断获取到的该注解类上的注解是否是排除注解
	 * @param aClass 注解
	 * @param annotations 排除注解集合
	 * @return 布尔值
	 */
	private static boolean is(Class<? extends Annotation> aClass,Set<Class< ? extends Annotation>> annotations) {
		for (Class<? extends Annotation> annotation: annotations) {
			if (aClass == annotation) return true;
		}
		return false;
	}

	/**
	 * 判断该类上是否具有该注解
	 * <p>**该注解的所有扩展注解使用的其他注解必须一致,扩展注解的复合注解一样适用**</p>
	 * @param annotatedElement 继承AnnotatedElement类
	 * @param annotationClass 注解
	 * @param annotations 排除注解集合
	 * @return 布尔值
	 */
	public static boolean isAnnotation(AnnotatedElement annotatedElement, Class< ? extends Annotation> annotationClass,Set<Class< ? extends Annotation>> annotations) {
		// 注解判断:判断该类上是否存在指定类型的注解
		if (annotatedElement.isAnnotationPresent(annotationClass)) return true;
		// 获取该类上所有注解
		for (Annotation annotation : annotatedElement.getAnnotations()) {
			// 注解class类对象
			Class<? extends Annotation> aClass = annotation.annotationType();
			// 过滤
			if (!is(aClass,annotations)) continue;
			// 递归探查
			return isAnnotation(aClass,annotationClass,annotations);
		}
		return false;
	}

	/**
	 * 判断该类上是否具有该注解,若存在则获取
	 * @param annotatedElement 继承AnnotatedElement类
	 * @param annotationClass 注解
	 * @param annotations 排除注解集合
	 * @return 注解
	 * @since 1.1.0
	 */
	public static Annotation getAnnotation(AnnotatedElement annotatedElement, Class< ? extends Annotation> annotationClass,Set<Class< ? extends Annotation>> annotations) {
		if (isAnnotation(annotatedElement,annotationClass,annotations)) return annotatedElement.getAnnotation(annotationClass);
		return null;
	}
}