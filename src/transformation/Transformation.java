package transformation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import register.SourceFileAnalyzer;
import search.MethodInfo;
import search.Search;

public class Transformation {
	public static void main(String[] args) throws Exception {
		Transformation tr = new Transformation();
		tr.execute(args);
	}

	public void execute(String[] args) throws Exception {
		GenProg gen = new GenProg();
		/*
		 * String[] arguments = { "-location", "./commons-text-1.0", "-mode",
		 * "jgenprog", "-scope", "global", "-failing",
		 * "org.apache.commons.text.StrBuilderTest", "-srcjavafolder",
		 * "/src/main/java/", "-srctestfolder", "/src/test/", "-binjavafolder",
		 * "/target/classes", "-bintestfolder", "/target/test-classes",
		 * "-flthreshold", "0.5", "-seed", "4", "-maxtime", "100", "-stopfirst",
		 * "true", "-dependencies",
		 * "./commons-text-1.0/lib/hamcrest-all-1.3.jar:./commons-text-1.0/lib/hamcrest-core-1.3.jar;./commons-text-1.0/lib/junit-4.12.jar;./commons-text-1.0/lib/commons-lang3.jar"
		 * };
		 */
		gen.execute(args);
	}

	/**
	 * メソッドAのコードをメソッドBのコードに置き換える
	 *
	 * @param methodA
	 *            書き換え先
	 * @param methodB
	 *            書き換え元
	 * @return replacedSourceCode 書き換え後のソースコード全体
	 */
	public String replaceCode(MethodInfo methodA, MethodInfo methodB) throws IOException {
		SourceFileAnalyzer sfa = new SourceFileAnalyzer();
		CompilationUnit unitA = sfa.getAST(methodA.getFilePath());
		CompilationUnit unitB = sfa.getAST(methodB.getFilePath());
		/*
		 * String packageA = unitA.getPackage().toString();//import文の追加 String
		 * packageB = unitB.getPackage().toString(); if
		 * (!packageA.equals(packageB)) { if (!existsImport(unitA, methodB)) {
		 * addImport(unitA, methodA, methodB); } }
		 */
		ReplaceVisitor visitorA = new ReplaceVisitor(unitA, methodA);
		visitorA.doReplace();
		ReplaceVisitor visitorB = new ReplaceVisitor(unitB, methodB);
		unitB.accept(visitorB);
		visitorA.setReplacement(visitorB.getMethodNode());
		unitA.accept(visitorA);
		ASTRewrite rewriter = visitorA.getRewriter();
		String source = Files.lines(Paths.get(methodA.getFilePath()), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		String replacedSourceCode = getCode(source, rewriter);

		return replacedSourceCode;
	}

	/**
	 * メソッドAのコードを，メソッドBを含むクラスに移植する
	 *
	 * @param methodA
	 *            書き換え先
	 * @param methodB
	 *            書き換え元
	 * @return replacedSourceCode 書き換え後のソースコード全体
	 * @throws IOException
	 */
	public String replaceCode2(MethodInfo methodA, MethodInfo methodB) throws IOException {
		String replacedSourceCode = null;
		SourceFileAnalyzer sfa = new SourceFileAnalyzer();
		CompilationUnit unitA = sfa.getAST(methodA.getFilePath());
		CompilationUnit unitB = sfa.getAST(methodB.getFilePath());

		ReplaceVisitor2 visitor = new ReplaceVisitor2(unitB, methodA, methodB);
		unitB.accept(visitor);
		unitA.accept(visitor);
		ASTRewrite rewriter=visitor.getRewriter();
		String source = Files.lines(Paths.get(methodB.getFilePath()), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		replacedSourceCode = getCode(source, rewriter);


		return replacedSourceCode;
	}

	/**
	 * メソッドAのコードを，メソッドBを含むクラスに移植する
	 *
	 * @param methodA
	 *            書き換え先
	 * @param methodB
	 *            書き換え元
	 * @return replacedSourceCode 書き換え後のソースコード全体
	 * @throws IOException
	 */
	public String replaceCode3(MethodInfo methodA, MethodInfo methodB) throws IOException {
		String replacedSourceCode = null;
		SourceFileAnalyzer sfa = new SourceFileAnalyzer();
		CompilationUnit unitA = sfa.getAST(methodA.getFilePath());
		CompilationUnit unitB = sfa.getAST(methodB.getFilePath());

		ReplaceVisitor3 visitor = new ReplaceVisitor3(unitA, methodA, methodB);
		unitA.accept(visitor);
		unitB.accept(visitor);
		ASTRewrite rewriter=visitor.getRewriter();
		String source = Files.lines(Paths.get(methodA.getFilePath()), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		replacedSourceCode = getCode(source, rewriter);


		return replacedSourceCode;
	}

	/**
	 * ASTからソースコードを復元
	 *
	 * @param ソースコード，AST
	 * @return ソースコード
	 */
	public String getCode(String code, ASTRewrite rewriter) {
		IDocument eDoc = new Document(code);
		// TextEdit edit = unit.rewrite(eDoc, null);
		TextEdit edit = rewriter.rewriteAST(eDoc, null);
		try {
			edit.apply(eDoc);
			return eDoc.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean existsImport(CompilationUnit unit, MethodInfo method) {
		boolean exist = false;
		List<ImportDeclaration> ids = unit.imports();
		for (ImportDeclaration id : ids) {
			if (id.getName().toString().equals(method.getClassName())) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	public void addImport(CompilationUnit unit, MethodInfo methodA, MethodInfo methodB) throws IOException {
		Document doc = new Document(Files.lines(Paths.get(methodA.getFilePath()), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator"))));
		AST ast = unit.getAST();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(methodB.getClassName()));
		unit.imports().add(id);
		unit.rewrite(doc, null);// うまくいかない
	}
}
