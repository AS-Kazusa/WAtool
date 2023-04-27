package kazusa.fileio;

import kazusa.common.SyntacticSugar;
import kazusa.string.StringUtil;

import java.io.File;
import java.io.IOException;


/**
 * @author kazusa
 * @version 1.1.0
 */
public class FileIOUtil {

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
			return SyntacticSugar.tryCatch(() -> new File(path).getCanonicalPath());
		}
	}
}