package kazusa.fileio;

import kazusa.common.SyntacticSugar;
import kazusa.string.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static kazusa.fileio.IOUtil.getInputThoroughfare;
import static kazusa.fileio.IOUtil.path;

/**
 * 文件
 * @author kazusa
 * @version 1.1.0
 * @see FileIOUtil
 */
public class FileUtil {

	private static File file;

	/**
	 * 获取一个多级目录或文件或多级目录文件
	 * @param path 路径
	 * @param fileName 文件
	 */
	public static void newPathFile(String path,String fileName) {
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
		SyntacticSugar.tryCatch(() -> {
			if (!file.createNewFile()) throw new FileNotFoundException("文件创建失败");
		});
	}

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
		Map<String, String> fileMap = new HashMap<>();
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
	 * jar文件操作
	 */
	public static class jarFile {

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
				}
			}
			return null;
		}
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
}
