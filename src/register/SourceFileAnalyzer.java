package register;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * ソースファイルの解析をするクラス
 * @author s-kento
 */

public class SourceFileAnalyzer {
	private List<String> fileList = new ArrayList<String>(); // ターゲットファイル群．ファイルの絶対パスを格納

	public SourceFileAnalyzer(File file) throws IOException { // コンストラクタ
		setFileList(file);
	}
	public SourceFileAnalyzer(){

	}


	/**
	 * 指定したディレクトリ下のファイル名(絶対パス)をfileList変数に格納する
	 *
	 * @param file ターゲットディレクトリ
	 * @throws IOException
	 */
	public void setFileList(File file) throws IOException {
		this.fileList =  Files.walk(file.toPath())
				.filter(e -> !Files.isDirectory(e))
				.filter(e -> e.toString().endsWith(".java"))
				.map(e -> e.toString())
				.collect(Collectors.toList());
	}

	/*
	 * fileListを返す
	 *
	 * @return fileList
	 */
	public List<String> getFileList() {
		return fileList;
	}

	/**
	 * ソースコードのASTを返す
	 *
	 * @param filePath ファイルの絶対パス
	 *
	 * @return CompolationUnit ソースコードのAST
	 */
	public CompilationUnit getAST(String filePath) throws IOException {
		String source = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)
				.collect(Collectors.joining(System.getProperty("line.separator")));
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map options = JavaCore.getOptions();
		parser.setCompilerOptions(options);
		parser.setEnvironment(Envs.getClassPath(), Envs.getSourcePath(), null, true);
		parser.setSource(source.toCharArray());
		parser.setUnitName("Target.java");
		CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		unit.recordModifications();
		/*String source = Files.lines(Paths.get(filePath), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());*/
		return unit;
	}
}
