package transformation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/*
 * メソッド書き換えやリコンパイル，メソッド実行を担当するクラス
 */
public class Controller {
	/*
	 * jarファイルから指定したjavaファイルを抽出する
	 *
	 * @param fileName 抽出したいJavaファイル
	 */
	public void extractJavaFile(String jarFileName, String javaFileName) {
		File file = new File("work\\" + jarFileName);
		try (JarFile jarFile = new JarFile(file)) {
			printFile(jarFile, javaFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * jarファイルからエントリを抽出し，文字列として読み込んで新規ファイルに書き込む
	 *
	 * @param zipFile jarファイル
	 *
	 * @param name 抽出したいJavaファイル
	 */
	private void printFile(ZipFile zipFile, String name) throws IOException {
		File file = new File("work\\" + name);
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		ZipEntry ze=null;
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
		     ze = entries.nextElement();
		    if (new File(ze.getName()).getName().equals(name)) {
		        break;
		    }
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))) {
			for (;;) {
				String text = reader.readLine();
				if (text == null) {
					break;
				}
				//System.out.println(text);
				pw.println(text);
			}
		}
		pw.close();
	}

	/*
	 * 書き換えたメソッドを含むjavaファイルをコンパイルし，クラスファイルを生成する
	 *
	 * @param zipFile jarファイル
	 *
	 * @param name 抽出したいJavaファイル
	 */
	public void compile(String jarFileName, String javaFileName) {
		File src = new File("work\\" + javaFileName);
		String[] args = { "-classpath", "C:\\pleiades\\workspace\\AutoProgramming\\work\\" + jarFileName,
				src.getAbsolutePath() };
		JavaCompiler c = ToolProvider.getSystemJavaCompiler();
		int r = c.run(null, null, null, args);
		if (r != 0)
			throw new RuntimeException("コンパイル失敗:" + r);
	}

	/*
	 * メソッドを実行する
	 * @param jarFileName jarファイル名
	 * @param javaFileName javaファイル名
	 * @param packageName パッケージ名
	 * @param className クラス名
	 * @param methodName メソッド名
	 * @param parameterName 引数の型
	 * @param args メソッドの引数
	 * @return r メソッドの戻り値
	 */
	public Object run(String jarFileName, String javaFileName, String packageName, String className, String methodName,
			String parameterName, Object[] args) {
		File file1 = new File("C:\\pleiades\\workspace\\AutoProgramming\\work\\");
		File file2 = new File("C:\\pleiades\\workspace\\AutoProgramming\\work\\" + jarFileName);
		URLClassLoader load;
		Object r = null;
		try {
			load = URLClassLoader.newInstance(new URL[] { file1.toURI().toURL(), file2.toURI().toURL() });
			Class cl;
			cl = load.loadClass(packageName + "." + className);
			Method method = cl.getMethod(methodName,analyzeType(parameterName));
			r = method.invoke(cl.newInstance(), args);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | InstantiationException
				| MalformedURLException e) {
			e.printStackTrace();
		}
		return r;
	}

	/*
	 * カンマ区切りの型名から，Classクラスの配列を返す
	 * @param parameterTypes 引数の型，カンマ区切り
	 * @return classes Classクラスの配列
	 */
	public Class<?>[] analyzeType(String parameterTypes){
		if(parameterTypes.equals("null"))
			return null;
		String[] types=parameterTypes.split(",",0);
		Class<?>[] classes = new Class<?>[types.length];
		for(int i=0;i<types.length;i++){
			try {
				classes[i]=Class.forName(types[i]);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return classes;
	}

	/*
	 * ファイル名に.jarがついてなければつける
	 * @param original jarファイル名
	 * @return adjusted 拡張子付きのファイル名
	 */
	/*public String adjustJarFileName(String original){
		String adjusted;
		if(new File(original).getPath().endsWith(".jar")){
			adjusted=original;
		}else{
			adjusted=original+".jar";
		}
		return adjusted;
	}*/
}
