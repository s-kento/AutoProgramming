package transformation;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
		Search search = new Search();
		List<MethodInfo> methods = search.execute(args);
		/*
		 * ここらへんで，GenProgに渡す適当な引数を生成する(テストケース生成も含めて)：未実装
		 */
		GenProg gen = new GenProg();
		String[] arguments = { "-location", "./commons-text-1.0", "-mode", "jgenprog", "-scope", "global", "-failing",
				"org.apache.commons.text.StrBuilderTest", "-srcjavafolder", "/src/main/java/", "-srctestfolder",
				"/src/test/", "-binjavafolder", "/target/classes", "-bintestfolder", "/target/test-classes",
				"-flthreshold", "0.5", "-seed", "4", "-maxtime", "100", "-stopfirst", "true", "-dependencies",
				"./commons-text-1.0/lib/hamcrest-all-1.3.jar:./commons-text-1.0/lib/hamcrest-core-1.3.jar;./commons-text-1.0/lib/junit-4.12.jar;./commons-text-1.0/lib/commons-lang3.jar" };
		gen.execute(arguments);
	}

	/**
	 * メソッドAのコードをメソッドBのコードに置き換える
	 *
	 * @param
	 */
	public ASTRewrite replaceCode(MethodInfo methodA, MethodInfo methodB) throws IOException {
		SourceFileAnalyzer sfa = new SourceFileAnalyzer();
		CompilationUnit unitA = sfa.getAST(methodA.getFilePath());
		CompilationUnit unitB = sfa.getAST(methodB.getFilePath());
		ReplaceVisitor visitorA = new ReplaceVisitor(unitA,methodA);
		visitorA.doReplace();
		ReplaceVisitor visitorB = new ReplaceVisitor(unitB,methodB);
		unitB.accept(visitorB);
		visitorA.setReplacement(visitorB.getMethodNode());
		unitA.accept(visitorA);
		System.out.println(unitA.toString());


		//return unitA;
		return visitorA.getRewriter();
	}

	/**
	 * ASTからソースコードを復元
	 * @param ソースコード，AST
	 * @return ソースコード
	 */
	public String getCode(String code, ASTRewrite rewriter) {
		IDocument eDoc = new Document(code);
		//TextEdit edit = unit.rewrite(eDoc, null);
		TextEdit edit = rewriter.rewriteAST(eDoc, null);
		try {
			edit.apply(eDoc);
			return eDoc.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
