package transformation;

import fr.inria.main.evolution.AstorMain;
import search.MethodInfo;

public class GenProg {
	public static void main(String[] args) throws Exception {
		GenProg gen = new GenProg();
		gen.execute(args);
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
}
