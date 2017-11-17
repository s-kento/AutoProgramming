package transformation;

import fr.inria.main.evolution.AstorMain;
import search.MethodInfo;

public class GenProg implements Runnable {
	String[] args;
	MethodInfo targetMethod = null;

	public GenProg(String[] args) {
		this.args=args;
	}

	public GenProg(String[] args, MethodInfo targetMethod) {
		this.args = args;
		this.targetMethod = targetMethod;
	}

	public void execute(String[] args) throws Exception {
		AstorMain astor = new AstorMain();
		astor.execute(args);
	}

	public void execute(String[] args, MethodInfo targetMethod) throws Exception {
		AstorMain astor = new AstorMain(targetMethod.getClassName(), targetMethod.getMethodName(),
				targetMethod.getStartLine(), targetMethod.getEndLine());
		astor.execute(args);
	}

	public void run() {
		try {
			if(null!=targetMethod)
				execute(args, targetMethod);
			else
				execute(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
