package register;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import search.MethodInfo;
import search.SQLite;
import suggestion.Suggestion;
import suggestion.entity.Method;

/*
 * メソッドのシグネチャを取得するためのvisitor
 * メソッド情報も持つ
 * @author s-kento
 */
public class MyVisitor extends ASTVisitor {
	private CompilationUnit unit;
	private String filePath;
	private String returnType;
	private String methodName;
	private List<String> parameterType = new ArrayList<String>();
	private String className;
	private String projectName;
	private int startLine;
	private String sourceCode;

	private SQLite db;

	public MyVisitor(CompilationUnit unit, String filePath, String db, String table, String projectName)
			throws ClassNotFoundException, SQLException {// コンストラクタ
		this.unit = unit;
		setFilePath(filePath);
		setProjectName(projectName);
		this.db = new SQLite(db, table);
	}

	/************************* getterとsetter *********************************/
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<String> getParameterType() {
		return parameterType;
	}

	public void setParameterType(List<String> parameterType) {
		this.parameterType = parameterType;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	/******************************************************************/

	/*
	 * クラス名を取得
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.isInterface()) // インターフェースは無視
			return false;
		setClassName(getFQName(node));
		return true;
	}

	/*
	 * メソッドのシグネチャ情報を取得
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		if (!node.isConstructor() && node.getReturnType2() != null) { // コンストラクタ，ENUM宣言は無視する．
			setStartLine(unit.getLineNumber(node.getStartPosition() - 1));
			setSourceCode(toHexString(node.toString()));
			setMethodName(node.getName().toString());
			setReturnType(getFQName(node.getReturnType2()));
			List<SingleVariableDeclaration> pTypes = node.parameters();
			List<String> tmp = new ArrayList<String>();
			if (pTypes.size() == 0) {// 引数がない場合，nullと格納．
				tmp.add("null");
			}
			for (SingleVariableDeclaration pType : pTypes) {
				tmp.add(getFQName(pType.getType()));
			}
			Collections.sort(tmp);// パラメータをアルファベット順にしておく
			setParameterType(tmp);

			showMethodInfo(this); // メソッド情報を標準出力に表示

			// ここからメソッド間の距離の登録を行う
			boolean flag = false;
			try {
				MethodInfo method = new MethodInfo(this);
				Suggestion suggestion = new Suggestion(db);
				flag = suggestion.regist(method);
			} catch (DecoderException e) {
				e.printStackTrace();
			}
			if (flag) {
				try {// メソッド情報をデータベースに登録
					db.register(this);
					System.out.println(getMethodName() + "はDBに追加しました");
					System.out.println("\n");
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(getMethodName() + "はDBに追加しません");
				System.out.println("\n");
			}
		}
		return true;
	}

	/*
	 * メソッド情報を標準出力に表示する
	 *
	 * @param visitor
	 */
	public void showMethodInfo(MyVisitor visitor) {
		List<String> parameterType = visitor.getParameterType();
		System.out.println("クラス名：" + visitor.getClassName());
		System.out.println("返値の型：" + visitor.getReturnType());
		System.out.println("メソッド名：" + visitor.getMethodName());
		System.out.print("パラメータの型：");
		for (int i = 0; i < parameterType.size(); i++) {
			System.out.print(parameterType.get(i));
			if (i < parameterType.size() - 1) {
				System.out.print(",");
			}
		}
		System.out.print("\n");
	}

	/*
	 * 完全限定名を取得
	 *
	 * @param ASTNode node
	 *
	 * @retun String
	 */
	public String getFQName(TypeDeclaration node) {
		ITypeBinding itb = node.resolveBinding();
		String packageName = itb.getPackage().getName().toString();
		String fqName = node.getName().toString();
		if (packageName != null) {
			fqName = packageName + "." + fqName;
		}

		return fqName;
	}

	/*
	 * 完全限定名を取得
	 *
	 * @param ASTNode node
	 *
	 * @retun String
	 */
	public String getFQName(Type node) {
		ITypeBinding itb = node.resolveBinding();
		String packageName = null;
		;
		if (itb.getPackage() != null)
			packageName = itb.getPackage().getName().toString();
		String fqName = node.toString();
		if (packageName != null) {
			fqName = packageName + "." + fqName;
		}

		return fqName;
	}

	/*
	 * String型の文字列を16進数文字列に変換
	 *
	 * @param str String型の文字列
	 *
	 * @return hexstr 16進数文字列
	 */
	public String toHexString(String str) {
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String hexstr = new String(Hex.encodeHex(bytes));

		return hexstr;
	}
}
