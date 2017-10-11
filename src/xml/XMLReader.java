package xml;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * eclemmaのxmlファイルを解析するクラス
 *
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
	int lineNumber;
	List<CoverageInfo> coverages = new ArrayList<>();

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		saxParser.parse(new File("commons-math.xml"), new XMLReader());
	}

	public void startDocument() {
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
					lineNumber = Integer.parseInt(attributes.getValue("line"));
					isMethod = true;
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
				//System.out.println(getAbsoluteClassName(className) + " " + methodName + ":" + coverage * 100 + "%");
				if ((double) 1 == coverage)
					perfectCoverage++;
				CoverageInfo coverageInfo = new CoverageInfo(getAbsoluteClassName(className), methodName, lineNumber, coverage);
				coverages.add(coverageInfo);
			}
		}
	}

	public void endDocument() {// [50]
		System.out.println("[51] ドキュメント終了");
		//System.out.println("メソッド数： " + methodNumber);
		//System.out.println(perfectCoverage);
		CoverageRegister register = new CoverageRegister();
		try {
			try {
				register.regist(coverages);
			} catch (ParseException | DecoderException | IOException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 *'/'を'.'に置換
	 *内部クラスの表記,'$'を除去(クラス名があるならばそれに，匿名クラスならスーパークラスの名前にする)
	 * @param className
	 * @return 絶対クラス名
	 */
	public String getAbsoluteClassName(String className) {
		String absClassName = className;
		final String regex1 = "/";
		final Pattern p1 = Pattern.compile(regex1);
		final Matcher m1 = p1.matcher(absClassName);
		absClassName = m1.replaceAll("\\.");
		final int index = absClassName.lastIndexOf(".");
		final String packageName = absClassName.substring(0, index);
		String classNameWithoutPackage = absClassName.substring(index + 1);
		String[] splitedClassName = classNameWithoutPackage.split("\\$");
		if (splitedClassName.length > 1) {
			if (Pattern.matches("^[0-9]", splitedClassName[1])) {
				classNameWithoutPackage = splitedClassName[0];
			} else {
				classNameWithoutPackage = splitedClassName[1];
			}
		}

		return packageName + "." + classNameWithoutPackage;
	}

}
