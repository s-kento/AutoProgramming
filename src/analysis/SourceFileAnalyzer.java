package analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SourceFileAnalyzer {
	private List<String> fileList = new ArrayList<String>();

	SourceFileAnalyzer(String file){
		setFileList(new File(file));
	}

	public void setFileList(File file) {
		if (file.isDirectory()) {
			File[] innerFiles = file.listFiles();
			for (File tmp : innerFiles) {
				setFileList(tmp);
			}
		} else if (file.isFile()) {
			if (file.getName().endsWith(".java")) {
				fileList.add(file.getAbsolutePath());
			}
		}
	}

	public List<String> getFileList() {
		return fileList;
	}

	public CompilationUnit getAST(String filePath) throws IOException {
		String source = Files.lines(Paths.get(filePath), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		return unit;
	}
}
