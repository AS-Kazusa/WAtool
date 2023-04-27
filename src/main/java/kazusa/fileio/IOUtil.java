package kazusa.fileio;


import kazusa.common.SyntacticSugar;
import kazusa.string.StringUtil;

import java.io.*;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kazusa
 * @version 1.0.0
 * @see FileIOUtil
 */
public class IOUtil {

	@Deprecated
	public static final String MAVEN_PATH = path("/src/main/resources","");

	@Deprecated
	public static final String MAVEN_JAVA_PATH = path("/src/main/java","");

	/**
	 * @param path 路径
	 * @param file 文件,无则null或空字符串或空格字符串表示即可
	 * @return 绝对路径
	 */
	@Deprecated
	public static String path(String path,String file) {
		try {
			// 判空处理
			if (StringUtil.isNull(file)) return new File("." + path).getCanonicalPath();
			return new File("." + path + file).getCanonicalPath();
		} catch (IOException e) {
			// 文件绝对路径不做处理
			return SyntacticSugar.tryCatch(() -> new File(path).getCanonicalPath());
		}
	}

	public static int read;

	public static String string;

	public static byte[] bytes = new byte[8];

	/**
	 * 二进制读取类似汉字等数据时采用UTF-8保存为3byte一个汉字
	 */
	public static byte[] characterBytes = new byte[3];

	public static byte[] mbs = new byte[1024];

	public static char[] chars = new char[8];

	public static char[] chars24Kb = new char[8 * 1024];

	/**
	 * 此类实现一个输出流，其中数据被写入字节数组。 缓冲区会在数据写入时自动增长。
	 * @param bytes 指定缓存区起始大小
	 * @return 数组输入流
	 */
	public static ByteArrayInputStream getByteArrayInputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * @return 数组输出流
	 */
	public static ByteArrayOutputStream getByteArrayOutputStream() {
		return new ByteArrayOutputStream();
	}

	/**
	 * 文件对象转文件流对象
	 * @param path 文件路径
	 * @return 文件流
	 * @throws FileNotFoundException
	 * @since 1.1.0
	 */
	public static FileInputStream getFileInputStream(String path) throws FileNotFoundException {
		return new FileInputStream(path);
	}

	public static BufferedInputStream getInputStream(String path) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(path));
	}

	public static BufferedOutputStream getOutputStream(String path) throws IOException {
	    return getOutputStream(path,false);
	}

	public static BufferedOutputStream getOutputStream(String path,boolean append) throws IOException {
		return new BufferedOutputStream(new FileOutputStream(path,append));
	}

	public static BufferedReader getReaderInputStream(String path) throws FileNotFoundException {
		return new BufferedReader(new FileReader(path));
	}

	public static BufferedWriter getWriterOutputStream(String path) throws IOException {
		return getWriterOutputStream(path,false);
	}

	public static BufferedWriter getWriterOutputStream(String path,boolean append) throws IOException {
		return new BufferedWriter(new FileWriter(path,append));
	}

	public static BufferedInputStream getSocketInputStream(Socket socket) throws IOException {
		return new BufferedInputStream(socket.getInputStream());
	}

	public static BufferedOutputStream getSocketOutputStream(Socket socket) throws IOException {
		return new BufferedOutputStream(socket.getOutputStream());
	}

	/**
	 * 输入转换
	 * @param in 字节输入流
	 * @return 返回buffer字符输入流
	 * @throws FileNotFoundException
	 */
	public static BufferedReader InputTransformation(InputStream in) throws FileNotFoundException {
		return InputTransformation(in,StandardCharsets.UTF_8);
	}

	/**
	 * 输入转换
	 * @param in 字节输入流
	 * @param charsetName 指定字节流按字符流方式处理,规定字符流一次传输位数为指定编码格式字符位数
	 * @return 返回buffer字符输入流
	 * @throws FileNotFoundException
	 */
	public static BufferedReader InputTransformation(InputStream in,String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(in,charsetName));
	}

	/**
	 * 输入转换
	 * @param in 字节输入流
	 * @param cs 指定字节流按字符流方式处理,规定字符流一次传输位数为指定编码格式字符位数
	 * @return 返回buffer字符输入流
	 * @throws FileNotFoundException
	 */
	public static BufferedReader InputTransformation(InputStream in, Charset cs) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(in,cs));
	}

	/**
	 * 输出转换
	 * @param out 字节输出流
	 * @return 返回buffer字符输出流
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter outputTransformation(OutputStream out) throws FileNotFoundException {
		return outputTransformation(out,StandardCharsets.UTF_8);
	}

	/**
	 * 输出转换
	 * @param out 字节输出流
	 * @param charsetName 指定字节流按字符流方式处理,规定字符流一次传输位数为指定编码格式字符位数
	 * @return 返回buffer字符输出流
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter outputTransformation(OutputStream out,String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		return new BufferedWriter(new OutputStreamWriter(out,charsetName));
	}

	/**
	 * 输出转换
	 * @param out 字节输出流
	 * @param cs 指定字节流按字符流方式处理,规定字符流一次传输位数为指定编码格式字符位数
	 * @return 返回buffer字符输出流
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter outputTransformation(OutputStream out, Charset cs) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(out,cs));
	}

	public static ObjectOutputStream getObjectOutputStream(String path) throws IOException {
		return getObjectOutputStream(path,false);
	}

	public static ObjectOutputStream getObjectOutputStream(String path,boolean append) throws IOException {
		return new ObjectOutputStream(getOutputStream(path,append));
	}

	public static ObjectInputStream getObjectInputStream(String path) throws IOException {
		return new ObjectInputStream(getInputStream(path));
	}

	/**
	 * @param path 路径
	 * @return 写通道
	 * @throws IOException
	 */
	public static FileChannel getInputThoroughfare(String path) throws IOException {
		return new FileInputStream(path).getChannel();
	}

	/**
	 * @param path 路径
	 * @return 读通道
	 * @throws IOException
	 */
	public static FileChannel getOutputThoroughfare(String path) throws IOException {
		return new FileOutputStream(path).getChannel();
	}

	/**
	 * @param path 路径
	 * @param model 工作模式
	 * r:只读输入模式
	 * rw:读写输入输出模式
	 * rws:进行写操作时同步刷新到磁盘,刷新内容和元数据
	 * rwd:进行写操作时同步刷新到磁盘,刷新内容
	 * @return 通道
	 * @throws FileNotFoundException
	 */
	public static FileChannel getThoroughfare(String path, String model) throws FileNotFoundException {
		return new RandomAccessFile(path, model).getChannel();
	}

	/**
	 * @return 通道
	 */
	public static ReadableByteChannel getThoroughfare() {
		return Channels.newChannel(System.in);
	}

	/**
	 * @param path 路径
	 * @param model 工作模式
	 * @param mode 缓冲区模式
	 * @param index 起始范围
	 * @param length 结束范围
	 * @return 直接缓冲区:将文件内指定范围字节数据加载到内存中
	 * @since 1.1.0
	 * @throws IOException
	 */
	public static MappedByteBuffer getDirectBuffer(String path, String model,FileChannel.MapMode mode,int index, int length) throws IOException {
		// 读写模式:FileChannel.MapMode.READ_WRITE
		return getThoroughfare(path,model).map(mode,index, length);
	}

	/**
	 * 下载
	 * @param path 下载路径
	 * @param data 数据(byte[] or string)
	 * @throws IOException
	 * @since 1.1.0
	 */
	public static <T> void download(String path, T data) throws IOException {
		download(path,List.of(data));
	}

	/**
	 * 下载
	 * @param path 下载路径
	 * @param dataS 数据集合(byte[] or string)
	 * @throws IOException
	 * @since 1.1.0
	 */
	public static <T> void download(String path, List<T> dataS) throws IOException {
		if (dataS.size() == 0) return;
		if (dataS.get(0) instanceof byte[]) {
			BufferedOutputStream out = IOUtil.getOutputStream(path);
			SyntacticSugar.tryCatchFinallyClose(() -> {
				for(T data: dataS) {
					out.write((byte[]) data);
				}
			},out);
			return;
		}
		if (dataS.get(0) instanceof String) {
			BufferedWriter writerOutputStream = IOUtil.getWriterOutputStream(path);
			SyntacticSugar.tryCatchFinallyClose(() -> {
				for(T data: dataS) {
					writerOutputStream.write((String) data);
				}
			},writerOutputStream);
		}
	}

	/**
	 * 字节流IO读取
	 * @param in 字节输入流
	 * @param out 字节输出流
	 * @param bytes 读取单位
	 */
	public static void inOutPutStream(InputStream in,OutputStream out,byte[] bytes) {
		SyntacticSugar.tryCatchFinallyClose(() -> {
			while ((read = in.read(bytes)) != -1) {
				// 进度条待开发~
				out.write(bytes, 0, read);
			}
		},in,out);
	}

	/**
	 * 字符流IO读取
	 * @param bufferedReader 字符输入流
	 * @param bufferedWriter 字符输出流
	 * @throws IOException
	 */
	public static void charInOutPutStream(BufferedReader bufferedReader,BufferedWriter bufferedWriter,char[] chars) throws IOException {
		SyntacticSugar.tryCatchFinallyClose(() -> {
			while ((read = bufferedReader.read(chars)) != -1) {
				// 进度条待开发~
				bufferedWriter.write(chars,0,read);
			}
		},bufferedWriter,bufferedReader);
	}

	/**
	 * 序列化非对象数据类型
	 */
	@Deprecated
	public static class ObjectOutputStream_ {

		private static ObjectOutputStream objectOutputStream;

		private static ObjectOutputStream_ objectOutputStream_ = new ObjectOutputStream_();

		public static ObjectOutputStream_ config(String path) throws IOException {
			objectOutputStream = IOUtil.getObjectOutputStream(path);
			return objectOutputStream_;
		}

		private String call = "";

		private static List<String> calls = new ArrayList<>();

		public static List<String> getCalls() {
			return calls;
		}

		public ObjectOutputStream_ writeInt(int i) throws IOException {
			objectOutputStream.writeInt(i);
			call += "int ->";
			calls.add("int");
			return objectOutputStream_;
		}

		public ObjectOutputStream_ writeDouble(double d) throws IOException {
			objectOutputStream.writeDouble(d);
			call += "double ->";
			calls.add("double");
			return objectOutputStream_;
		}

		public ObjectOutputStream_ writeChar(char c) throws IOException {
			objectOutputStream.writeChar(c);
			call += "char ->";
			calls.add("char");
			return objectOutputStream_;
		}

		public ObjectOutputStream_ writeBoolean(boolean b) throws IOException {
			objectOutputStream.writeBoolean(b);
			call += "boolean ->";
			calls.add("boolean");
			return objectOutputStream_;
		}

		public ObjectOutputStream_ writeUTF(String s) throws IOException {
			objectOutputStream.writeUTF(s);
			call += "string ->";
			calls.add("string");
			return objectOutputStream_;
		}

		/**
		 * 该方法不保存类型
		 * @param bytes 数组
		 * @throws IOException
		 */
		public ObjectOutputStream_ write(byte[] bytes) throws IOException {
			return write(bytes,0,bytes.length);
		}

		/**
		 * 截取部分保存，左闭右开
		 * @param bytes 数组
		 * @param index 起始位置
		 * @param length 结束位置
		 * @throws IOException
		 */
		public ObjectOutputStream_ write(byte[] bytes,int index,int length) throws IOException {
			objectOutputStream.write(bytes, index, length);
			call += "array ->";
			calls.add("array");
			return objectOutputStream_;
		}

		@Deprecated
		public ObjectOutputStream_ writeObject(Object o) throws IOException {
			objectOutputStream.writeObject(o.getClass());
			call += "object ->";
			calls.add("object");
			return objectOutputStream_;
		}

		public String Builder() {
			close(objectOutputStream);
			return call;
		}
	}

	/**
	 * 序列化对象
	 * @param objectOutputStream 序列化流
	 * @param aClassList 序列化对象集合
	 * @param aClasses 序列化对象
	 * @return 返回序列化对象数
	 * @throws IOException
	 */
	public static int writeObject(ObjectOutputStream objectOutputStream,List<Class<?>> aClassList,Class<?>... aClasses) throws IOException {
		try {
			int i = 0;
			for (Class<?> aClass : aClasses) {
				objectOutputStream.writeObject(aClass);
				i++;
			}
			if (aClassList == null) return i;
			for (Class<?> aClass : aClassList) {
				objectOutputStream.writeObject(aClass);
				i++;
			}
			return i;
		} finally {
			close(objectOutputStream);
		}
	}

	private static List<Object> objs;

	/**
	 * 反序列化非对象数据类型
	 * @param objectInputStream 反序列化流
	 * @param calls 序列化记录集合
	 * @return 返回反序列化集合
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<Object> read(ObjectInputStream objectInputStream,List<String> calls) throws IOException, ClassNotFoundException {
		objs = new ArrayList<>();
		for (String call : calls) {
			if ("int".equals(call)) objs.add(objectInputStream.readInt());
			if ("double".equals(call)) objs.add(objectInputStream.readDouble());
			if ("char".equals(call)) objs.add(objectInputStream.readChar());
			if ("boolean".equals(call)) objs.add(objectInputStream.readBoolean());
			if ("string".equals(call)) objs.add((objectInputStream.readUTF()));
			if ("array".equals(call)) objs.add(objectInputStream.readAllBytes());
			if ("object".equals(call)) objs.add(objectInputStream.readObject());
		}
		calls.clear();
		close(objectInputStream);
		return objs;
	}

	/**
	 * 反序列化对象
	 * @param objectInputStream 反序列化流
	 * @param i 序列化对象数
	 * @return 返回反序列化集合
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static List<Object> readObject(ObjectInputStream objectInputStream,int i) throws IOException, ClassNotFoundException {
		objs = new ArrayList<>();
		for (int j = 0; j < i; j++) {
			objs.add(objectInputStream.readObject());
		}
		close(objectInputStream);
		return objs;
	}

	/**
	 * IO流关流方法
	 * @param closeables 实现对象
	 */
	public static void close(Closeable... closeables) {
		SyntacticSugar.tryCatch(() -> {
			for (int i = closeables.length - 1; i >= 0;i--) {
				if (closeables[i] == null) {
					continue;
				}
				closeables[i].close();
			}
		});
	}
}