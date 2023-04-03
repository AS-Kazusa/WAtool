package kazusa.io;



import kazusa.common.codeoptimize.CodeOptimizeUtil;
import kazusa.string.StringUtil;

import java.io.*;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author kazusa
 * @version 1.0.0
 */
public class IOUtil {

	public static final String MAVEN_PATH = path("/src/main/resources","");

	public static final String MAVEN_JAVA_PATH = path("/src/main/java","");

	/**
	 * @param path 路径
	 * @param file 文件,无则null或空字符串或空格字符串表示即可
	 * @return 绝对路径
	 */
	public static String path(String path,String file) {
		try {
			// 判空处理
			if (StringUtil.isNull(file)) return new File("." + path).getCanonicalPath();
			return new File("." + path + file).getCanonicalPath();
		} catch (IOException e) {
			// 文件绝对路径不做处理
			return CodeOptimizeUtil.tryCatch(() -> new File(path).getCanonicalPath());
		}
	}

	private static File file;

	/**
	 * 获取一个多级目录或文件或多级目录文件
	 * @param path 路径
	 * @param fileName 文件
	 */
	public static void setPathFile(String path,String fileName) {
		file = new File(path);
		// 判断该路径是否存在
		if (!(file.exists() && file.isDirectory())) {
			// 创建多级目录
			if (!file.mkdirs()) {
				System.err.println("多级目录创建失败");
				return;
			}
		}
		// 判空处理
		if (StringUtil.isNull(fileName)) return;
		// 内存创建文件
		file = new File(file.getPath(),fileName);
		// 在磁盘执行
		CodeOptimizeUtil.tryCatch(() -> {
			if (!file.createNewFile()) throw new FileNotFoundException("文件创建失败");
		});
	}

	private static HashMap<String, String> fileMap;

	/**
	 * @param path 路径
	 * @param fileName 文件
	 * @return 该路径或文件信息集合
	 * @throws IOException
	 */
	public static Map<String,String> getPathFile(String path, String fileName) throws IOException {
		path = path.replace("\\", "/");
		// 判断该路径是否为文件路径
		if (path.lastIndexOf(".") != -1) {
			// 匹配路径最后一个/获取fileName
			int indexOf = path.lastIndexOf("/");
			if (indexOf != -1) fileName = path.substring(indexOf + 1);
		}
		file = new File(path);
		fileMap = new HashMap<>();
		fileMap.put("目录名:",file.getName());
		fileMap.put("目录绝对路径:",path(path,""));
		fileMap.put("目录绝对路径的父级路径:",new File(path(path,"")).getParent());
		// 判空处理
		if (StringUtil.isNull(fileName)) return fileMap;
		file = new File(path,fileName);
		// 判断程序是否存在读取该路径下文件权限
		if (file.canRead()) return fileMap;
		fileMap.clear();
		fileMap.put("文件绝对路径:",path(path,fileName));
		fileMap.put("文件规范路径:",file.getCanonicalPath());
		fileMap.put("文件名:",file.getName());
		try (FileChannel channel = getInputThoroughfare(path)) {
			fileMap.put("文件大小:",channel.size() + "byte");
		}
		fileMap.put("文件hash码:", String.valueOf(file.hashCode()));
		if (file.isFile()) fileMap.put("文件类型:","普通文件");
		if (file.isHidden()) fileMap.put("文件类型:","隐藏文件");
		String format = new SimpleDateFormat("yyyy年MM月dd日E H小时mm分ss秒").format(file.lastModified());
		fileMap.put("文件上次修改时间:",format);
		return fileMap;
	}

	/**
	 * 扫描文件目录
	 */
	public static class scanning {

		/**
		 * code:扫描本地包和jar包
		 * @throws IOException
		 */
		public static List<List<File>> classes() throws IOException {
			List<List<File>> files = new ArrayList<>();
			for (String path: System.getProperty("java.class.path").split(";")) {
				System.out.println(path);
				if (!path.endsWith(".jar")) {
					files.add(mateFiles(path, ".class", false));
					continue;
				}
				files.add(classJar(path));
			}
			return files;
		}

		/**
		 * 存储扫描到的文件对象集合
		 */
		private static List<File> files = new ArrayList<>();

		public static List<File> getFiles() {
			return files;
		}

		/**
		 * 指定扫描包下所有指定类型文件
		 * @param path 路径
		 * @param suffix 后缀
		 * @param is 是否排除指定后缀文件
		 * @throws IOException
		 */
		public static List<File> mateFiles(String path, String suffix,boolean is) throws IOException {
			File file = new File(path);
			// 获取该路径下的路径或文件对象
			File[] files = file.listFiles();
			if (files == null) return getFiles();
			for (File f : files) {
				// 获取绝对路径
				String fCanonicalPath = f.getCanonicalPath();
				file = new File(fCanonicalPath);
				// 判断是否为文件
				if (!file.isFile()) {
					mateFiles(fCanonicalPath,suffix,is);
					continue;
				}
				// 判断是否要排除指定后缀文件
				if(is) {
					if (!fCanonicalPath.endsWith(suffix)) getFiles().add(file);
				} else {
					if (fCanonicalPath.endsWith(suffix)) getFiles().add(file);
				}
			}
			return getFiles();
		}

		/**
		 * jar包
		 * @param path 路径
		 * @throws IOException
		 */
		public static List<File> classJar(String path) throws IOException {
			// JarURLConnection类通过JAR协议建立了一个访问 jar包URL的连接，可以访问这个jar包或者这个包里的某个文件
			try (JarFile jarFile = new JarFile(new File(path))) {
				// 得到该JarFile目录下所有项目
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					// 获取jar包下每一个class文件对象
					JarEntry jarEntry = entries.nextElement();
					// jar包下相对路径
					String jarEntryName = jarEntry.getName();
					// 不是class文件不予处理,jar包内META-INF目录无需处理
					if (!jarEntryName.endsWith(".class")) continue;
					//String replace = jarEntry.getName().replace("/", ".");
					//replace = replace.substring(0, replace.length() - 6);
					files.add(new File(new File("." + jarEntry.getName()).getCanonicalPath()));
				}
			}
			return files;
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
	 * @throws IOException
	 */
	public static MappedByteBuffer getDirectBuffer(String path, String model,FileChannel.MapMode mode,int index, int length) throws IOException {
		// 读写模式:FileChannel.MapMode.READ_WRITE
		return getThoroughfare(path,model).map(mode,index, length);
	}

	/**
	 * 字节流IO读取
	 * @param in 字节输入流
	 * @param out 字节输出流
	 * @param bytes 读取单位
	 */
	public static void inOutPutStream(InputStream in,OutputStream out,byte[] bytes) {
		CodeOptimizeUtil.tryCatchFinallyClose(() -> {
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
		CodeOptimizeUtil.tryCatchFinallyClose(() -> {
			while ((read = bufferedReader.read(chars)) != -1) {
				// 进度条待开发~
				bufferedWriter.write(chars,0,read);
			}
		},bufferedWriter,bufferedReader);
	}

	/**
	 * 序列化非对象数据类型
	 */
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
		CodeOptimizeUtil.tryCatch(() -> {
			for (int i = closeables.length - 1; i >= 0;i--) {
				if (closeables[i] == null) {
					continue;
				}
				closeables[i].close();
			}
		});
	}
}