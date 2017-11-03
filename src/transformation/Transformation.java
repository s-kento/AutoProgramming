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
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import experiment.TestCaseChecker;
import register.SourceFileAnalyzer;
import search.MethodInfo;

public class Transformation {
	public static void main(String[] args) throws Exception {
		Transformation tr = new Transformation();
		tr.execute(args);
	}

	public void execute(String[] args) throws Exception {
		GenProg gen = new GenProg(args);
		gen.start();
		gen.join(1800000);
		if(gen.isAlive())
			gen.stop();
	}

	public void execute(String[] args, MethodInfo targetMethod) throws Exception {
		GenProg gen = new GenProg(args, targetMethod);
		gen.start();
		gen.join(1800000);
		if(gen.isAlive())
			gen.stop();
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
		ASTRewrite rewriter = visitor.getRewriter();
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

		ReplaceVisitor3 visitor = new ReplaceVisitor3(unitA, unitB, methodA, methodB);
		unitA.accept(visitor);
		visitor.firstSearch = false;
		unitB.accept(visitor);
		ASTRewrite rewriter = visitor.getRewriter();
		if (!methodA.getClassName().equals(methodB.getClassName())) {
			addImport(unitA, unitB, methodA, methodB, rewriter);
		}
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

	public void addImport(CompilationUnit unitA, CompilationUnit unitB, MethodInfo methodA, MethodInfo methodB, ASTRewrite rewriter)
			throws IOException {
		AST astA = unitA.getAST();
		ListRewrite lrw = rewriter.getListRewrite(unitA, unitA.IMPORTS_PROPERTY);
		List<ImportDeclaration> idA = unitA.imports();
		List<ImportDeclaration> idB = unitB.imports();
		for (ImportDeclaration id : idB) {
			if (!containsIDNode(idA, id)) {
				lrw.insertLast(id, null);
			}
		}

		String packageA = TestCaseChecker.getPackageName(methodA);
		String packageB = TestCaseChecker.getPackageName(methodB);
		if (!packageA.equals(packageB)) {
			ImportDeclaration id = astA.newImportDeclaration();
			id.setName(astA.newName(methodB.getClassName()));
			if(!containsIDNode(idA, id)){
				lrw.insertLast(id, null);
			}
		}

	}

	public boolean containsIDNode(List<ImportDeclaration> idList, ImportDeclaration targetId) {
		boolean contain = false;
		for(ImportDeclaration id : idList){
			if(id.getName().toString().equals(targetId.getName().toString())){
				contain=true;
				break;
			}
		}
		return contain;
	}

}
