package transformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import search.MethodInfo;

public class TestMaker {

	public void makeJUnitFile(MethodInfo method, Object expected, Object args[]) {
		String className = getClassName(method.getClassName());
		File junitFile = new File(className + "Test.java");
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(junitFile)));
			pw.println("package " + getPackageName(method.getClassName()) + ";");
			pw.println("import static org.junit.Assert.*;");
			pw.println("import org.junit.Test;");
			pw.println("public class " + className + "Test {");
			pw.println("public void test"+method.getMethodName()+"(){");
			pw.println(className+" method = new "+className+"();");
			pw.print("assertEquals("+expected.toString()+", method."+method.getMethodName()+"(");
			for(int i=0;i<args.length;i++){
				pw.print(args[i].toString());
				if(args.length>1&&i<args.length-1)
					pw.print(", ");
			}
			pw.println(");}}");
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("テストケース作成完了");

	}

	/*
	 * PackageName.ClassNameからClassNameを取り出す
	 *
	 * @param fileName PackageName.ClassName
	 *
	 * @return className
	 */
	public String getClassName(String fileName) {
		String className = null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			className = fileName.substring(point + 1);
		}
		return className;
	}

	/*
	 * PackageName.ClassNameからPackageNameを取り出す
	 *
	 * @param fileName PackageName.ClassName
	 *
	 * @return packageName
	 */
	public String getPackageName(String fileName) {
		String packageName = null;
		int point = fileName.lastIndexOf(".");
		if (point != -1) {
			packageName = fileName.substring(0, point);
		}
		return packageName;
	}

}
