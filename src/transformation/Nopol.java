package transformation;

public class Nopol implements Runnable {
	String args[];
	public Nopol(String[] args){
		this.args=args;
	}

	public void run() {
		fr.inria.lille.repair.Main.execute(args);
	}
}
