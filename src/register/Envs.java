package register;

import java.io.File;

public class Envs {

	public static final String UTF_8 = "UTF-8";
	public static final String SJIS = "SJIS";
	public static final String EUC_JP = "EUC-JP";

	public static String[] getClassPath() {
		String property = System.getProperty("java.class.path", ".");
		return property.split(File.pathSeparator);
	}

	public static String[] getSourcePath() {
		return new String[] { "." };
	}

	public static String getEncoding() {
		return UTF_8;
	}

	public static String getLineSeparator() {
		return System.getProperty("line.separator", "\n");
	}
}