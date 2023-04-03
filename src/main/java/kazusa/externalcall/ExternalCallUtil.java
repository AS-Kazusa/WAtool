package kazusa.externalcall;

import kazusa.common.codeoptimize.CodeOptimizeUtil;
import kazusa.io.IOUtil;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.List;

import static kazusa.io.IOUtil.getReaderInputStream;
import static kazusa.io.IOUtil.string;

/**
 * 外部调用
 * @author kazusa
 * @version 1.0.0
 */
public class ExternalCallUtil {

	private static Process exec;

	/**
	 * cmd调用
	 * @param order cmd命令
	 * @param file 指定工作环境和目录中执行cmd命令
	 * @param parameter 命令参数
	 * @throws IOException
	 */
	public static Process cmdCall(String order,File file,String... parameter) throws IOException {
		if (parameter.length > 0) {
			if (file == null) return Runtime.getRuntime().exec(order,parameter);
			return Runtime.getRuntime().exec(order,parameter,file);
		}
		return Runtime.getRuntime().exec(order);
	}

	/**
	 * cmd调用
	 * @param orders 多条cmd命令
	 * @param file 指定工作环境和目录中执行cmd命令
	 * @param parameter 命令参数
	 * @throws IOException
	 */
	public static Process cmdCall(String[] orders,File file,String... parameter) throws IOException {
		if (parameter.length > 0) {
			if (file == null) return Runtime.getRuntime().exec(orders,parameter);
			return Runtime.getRuntime().exec(orders,parameter,file);
		}
		return Runtime.getRuntime().exec(orders);
	}

	/**
	 * cmd交互
	 * @param exec 传入cmd对象
	 * @return 字符流
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static BufferedReader cmdEachOther(Process exec) throws FileNotFoundException, UnsupportedEncodingException {
		return IOUtil.InputTransformation(exec.getInputStream(),"GBK");
	}

	public static void cmdClose(Process exec) {
		// 终止子进程
		exec.destroy();
	}

	private static BufferedReader bufferedReader;

	private static StringBuilder stringBuilder = new StringBuilder();

	/**
	 * 调用js函数
	 * @param js 传入脚本路径或js字符串
	 * @param functionName 调用函数名
	 * @param args 参数
	 * @return 返回执行结果
	 */
	public static Object jsEval(Object js, String functionName,Object... args) throws IOException {
		String function = scriptStr(js);
		String engineName = engines().get(2);
		// 多个函数调用指定其中一个函数???
		// 判断该函数是否要传参
		if (function.contains("()")) return scriptCall(engineName,function,functionName,false);
		return scriptCall(engineName,function,functionName,true,args);
	}

	/**
	 * @return 支持java支持调用脚本语言(Java持有的解析引擎)列表
	 */
	public static List<String> engines() {
		return new ScriptEngineManager().getEngineFactories().get(0).getNames();
	}

	/**
	 * @param script 脚本对象只能为File||String对象
	 * @return 返回字符串形式脚本
	 * @throws IOException
	 */
	private static String scriptStr(Object script) throws IOException {
		if (script instanceof File) {
			bufferedReader = getReaderInputStream(((File) script).getCanonicalPath());
			CodeOptimizeUtil.tryCatchFinallyClose(() -> {
				// 读取脚本文件中调用函数
				while ((string = bufferedReader.readLine()) != null) {
					stringBuilder.append(string);
				}
			},bufferedReader);
			script = String.valueOf(stringBuilder);
		}
		return String.valueOf(script);
	}

	/**
	 * 解析引擎
	 */
	private static ScriptEngine engine;

	/**
	 * 脚本语言对象
	 */
	private static FileReader fReader = null;

	/**
	 * java调用脚本语言
	 * @param engineName   引擎名
	 * @param script    脚本字符串
	 * @param function 调用函数
	 * @param isArgs   设置是否开启传参
	 * @param args     参数
	 * @return 返回执行结果
	 */
	public static Object scriptCall(String engineName,String script, String function,boolean isArgs, Object... args) {
		return CodeOptimizeUtil.tryCatchFinallyClose(() -> {
			// 获取解析引擎
			engine = new ScriptEngineManager().getEngineByName(engineName);
			// 将脚本函数字符串传入解析引擎执行
			engine.eval(script);
			// 调用js中的方法,判断该方法是否需要传参
			if (isArgs) return ((Invocable) engine).invokeFunction(function, args);
			return ((Invocable) engine).invokeFunction(function);
		},fReader);
	}
}
