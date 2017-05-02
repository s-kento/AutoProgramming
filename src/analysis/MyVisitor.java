package analysis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import db.SQLite;

/*
 * メソッドのシグネチャを取得するためのvisitor
 * @author s-kento
 */
public class MyVisitor extends ASTVisitor {
	private CompilationUnit unit;
	private String filePath;
	private String returnType;
	private String methodName;
	private List<String> parameterType = new ArrayList<String>();
	private String className;
	private SQLite db;

	public MyVisitor(CompilationUnit unit, String filePath, String db, String table)
			throws ClassNotFoundException, SQLException {// コンストラクタ
		this.unit = unit;
		setFilePath(filePath);
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

	/******************************************************************/

	/*
	 * クラス名を取得
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		setClassName(node.getName().toString());
		return true;
	}

	/*
	 * メソッドのシグネチャ情報を取得
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		if (!node.isConstructor() && node.getReturnType2()!=null) { // コンストラクタ，ENUM宣言は無視する．
			setMethodName(node.getName().toString());
			setReturnType(node.getReturnType2().toString());
			List<SingleVariableDeclaration> pTypes = node.parameters();
			List<String> tmp = new ArrayList<String>();
			if (pTypes.size() == 0) {// 引数がない場合，nullと格納．
				tmp.add("null");
			}
			for (SingleVariableDeclaration pType : pTypes) {
				tmp.add(pType.getType().toString());
			}
			Collections.sort(tmp);// パラメータをアルファベット順にしておく
			setParameterType(tmp);

			showMethodInfo(this); // メソッド情報を標準出力に表示
			try {// メソッド情報をデータベースに登録
				db.register(this);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
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
		System.out.println("\n");
	}
}
