package kazusa.common.codeoptimize.strategicmode.prototype;

import kazusa.common.codeoptimize.CodeOptimizeUtil;

import java.io.*;

/**
 * 深拷贝
 * @author kazusa
 * @version 1.0.0
 */
public class DeepCopy {

	private static ByteArrayOutputStream byteArrayOutputStream;

	private static ObjectOutputStream objectOutputStream;

	private static ByteArrayInputStream byteArrayInputStream;

	private static ObjectInputStream objectInputStream;

	/**
	 * 深拷贝:弥补浅拷贝对象只能拷贝基本类型的问题,可拷贝引用类型
	 * @param serializable 序列化对象
	 * @return copy对象
	 */
	public static Serializable copy(Serializable serializable) {
		return CodeOptimizeUtil.tryCatch(() -> CodeOptimizeUtil.tryFinallyClose(() -> {
					byteArrayOutputStream = new ByteArrayOutputStream();
					// 输出流
					objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
					// 当前对象输出到内存
					objectOutputStream.writeObject(serializable);
					// 输入此流内置缓冲区
					byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
					// 输入流
					objectInputStream = new ObjectInputStream(byteArrayInputStream);
					// 返回拷贝对象
					return (Serializable) objectInputStream.readObject();
				},byteArrayOutputStream, objectOutputStream, byteArrayInputStream, objectInputStream));
	}
}
