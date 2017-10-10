package xml;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * eclemmaのxmlファイルを解析するクラス
 * @author s-kento
 *
 */
public class XMLReader extends DefaultHandler {
	String className;
	String methodName;
	double coverage;
	boolean isInMain;
	boolean isMethod;
	int methodNumber = 0;
	int perfectCoverage = 0;

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		saxParser.parse(new File("commons-math.xml"), new XMLReader());
	}

	public void startDocument() {// [10]
		System.out.println("[11] ドキュメント開始");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if ("group".equals(qName)) {
			if ("src/main/java".equals(attributes.getValue("name")))
				isInMain = true;
			else
				isInMain = false;
		}
		if (isInMain) {
			if ("class".equals(qName)) {
				className = attributes.getValue("name");
			}
			if ("method".equals(qName)) {
				if ("<init>".equals(attributes.getValue("name")) || "<clinit>".equals(attributes.getValue("name")))
					isMethod = false;
				else {
					methodName = attributes.getValue("name");
					isMethod=true;
					methodNumber++;
				}
			}
			if ("counter".equals(qName) && "INSTRUCTION".equals(attributes.getValue("type")) && isMethod) {
				double covered = Double.parseDouble(attributes.getValue("covered"));
				double missed = Double.parseDouble(attributes.getValue("missed"));
				coverage = covered / (covered + missed);
			}
		}
	}

	public void endElement(String uri, String localName, String qName) {
		if (isInMain) {
			if ("method".equals(qName) && isMethod) {
				System.out.println(getAbsoluteClassName(className) + " " + methodName + ":" + coverage * 100 + "%");
				if ((double) 1 == coverage)
					perfectCoverage++;
			}
		}
	}

	public void endDocument() {// [50]
		System.out.println("[51] ドキュメント終了");
		System.out.println("メソッド数： " + methodNumber);
		System.out.println(perfectCoverage);
	}

	public String getAbsoluteClassName(String className) {
		String absClassName = className;
		String regex = "/";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(absClassName);
		absClassName = m.replaceAll("\\.");
		int index = absClassName.lastIndexOf(".");
		String packageName = absClassName.substring(0, index);
		String classNameWithoutPackage = absClassName.substring(index + 1);
		return packageName + "." + classNameWithoutPackage;
	}

}
