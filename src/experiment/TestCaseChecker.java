package experiment;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.io.FilenameUtils;

import search.MethodInfo;
import search.Search;

public class TestCaseChecker {
	public static void main(String[] args)
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		List<TestCaseInfo> testcases = new ArrayList<>();
		for (MethodInfo method : methods) {
			TestCaseInfo testcase = new TestCaseInfo(method);
			String[] argument = { "-r", method.getReturnType(), "-p", method.getParameterType(), "-m",
					method.getMethodName(), "-P", "commons-math" };
			List<MethodInfo> evolvedMethods = search.execute(argument);
			testcase.setSigNumber(evolvedMethods.size());
			if (!Main.existsTestFile(getClassName(method), getPackageName(method))) {
				testcases.add(testcase);
				break;
			}else{//テストケースが存在すれば
				
				
			}

		}
	}

	public static String getClassName(MethodInfo method) {
		String javaFileName = new File(method.getFilePath()).getName();
		String className = FilenameUtils.removeExtension(javaFileName);
		return className;
	}

	public static String getPackageName(MethodInfo method) {
		String absClassName = method.getClassName();
		String packageName = Main.toPackageName(absClassName);
		return packageName;
	}
}
