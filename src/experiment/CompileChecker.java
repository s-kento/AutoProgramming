package experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;

import search.MethodInfo;
import search.Search;
import transformation.Controller;
import transformation.Transformation;

public class CompileChecker {
	public static void main(String[] args)
			throws ClassNotFoundException, SQLException, ParseException, DecoderException, IOException {
		MethodGenerator mg = new MethodGenerator();
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		String targetJavaFileName;
		String projectJarFileName = "work\\commons-math\\target\\commons-math4-4.0-SNAPSHOT.jar";
		String dependencies = "work\\commons-numbers-core-1.0-SNAPSHOT.jar;work\\commons-numbers-gamma-1.0-SNAPSHOT.jar;"
				+ "work\\commons-numbers-angle-1.0-SNAPSHOT.jar;work\\commons-numbers-arrays-1.0-SNAPSHOT.jar;"
				+ "work\\commons-rng-client-api-1.0.jar;work\\commons-rng-simple-1.0.jar;work\\commons-rng-sampling-1.1-SNAPSHOT.jar;"
				+ "work\\jmh-core-1.13.jar;work\\jmh-generator-annprocess-1.13.jar;work\\junit-4.11.jar;"
				+ "work\\commons-numbers-fraction-1.0-SNAPSHOT.jar;work\\commons-rng-core-1.0.jar;work\\jopt-simple-4.6.jar;"
				+ "work\\commons-math3-3.2.jar;work\\hamcrest-core-1.3.jar;work\\commons-numbers-combinatorics-1.0-SNAPSHOT.jar";
		File file = new File("CompileCheck.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		pw.println("FilePath, MethodName, startLine, Candidate Number, Compilable Code Number");
		int ccn;
		for (MethodInfo targetMethod : methods) {
			ccn = 0;
			targetJavaFileName = new File(targetMethod.getFilePath()).getName();
			String[] argument = { "-r", targetMethod.getReturnType(), "-p", targetMethod.getParameterType(), "-m",
					targetMethod.getMethodName(), "-P", "commons-math" };
			List<MethodInfo> evolvedMethods = search.execute(argument);
			Transformation trans = new Transformation();
			Controller ctr = new Controller();
			for (MethodInfo evMethod : evolvedMethods) {
				if (targetMethod.equals(evMethod))
					continue;
				String replacedCode;
				if(targetMethod.getClassName().equals(evMethod.getClassName()))
					replacedCode = trans.replaceCode(targetMethod, evMethod);
				else
					replacedCode = trans.replaceCode3(targetMethod, evMethod);
				File targetJavaFile = new File("work\\" + targetJavaFileName);
				FileWriter filewriter = new FileWriter(targetJavaFile);
				filewriter.write(replacedCode);
				filewriter.close();
				int r = ctr.compile(projectJarFileName + ";" + dependencies, targetJavaFileName);
				if (r != 0) {
					mg.deleteFile(targetJavaFile.getAbsolutePath());
					continue;
				} else {
					ccn++;
					mg.deleteFile(targetJavaFile.getAbsolutePath());
				}
			}

			pw.println(targetMethod.getFilePath() + ", " + targetMethod.getMethodName() + ", "
					+ targetMethod.getStartLine() + ", "+evolvedMethods.size()+", "+ccn);
		}
		pw.close();
	}
}
