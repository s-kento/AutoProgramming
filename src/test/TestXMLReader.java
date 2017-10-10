package test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TestXMLReader extends DefaultHandler {
	String className;
	String methodName;
	double coverage;
	boolean isInMain;

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		saxParser.parse(new File("commons-math.xml"), new TestXMLReader());
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
			if ("method".equals(qName) && !"<init>".equals(attributes.getValue("name"))
					&& !"<clinit>".equals(attributes.getValue("name"))) {
				methodName = attributes.getValue("name");
			}
			if ("counter".equals(qName) && "INSTRUCTION".equals(attributes.getValue("type"))) {
				double covered = Double.parseDouble(attributes.getValue("covered"));
				double missed = Double.parseDouble(attributes.getValue("missed"));
				coverage = covered / (covered + missed);
			}
		}
	}

	public void endElement(String uri, String localName, String qName) {
		if (isInMain) {
			if ("method".equals(qName)) {
				System.out.println(getAbsoluteClassName(className) + " " + methodName + ":" + coverage * 100 + "%");
			}
		}
	}

	public void endDocument() {// [50]
		System.out.println("[51] ドキュメント終了");
	}

	public String getAbsoluteClassName(String className){
		String absClassName=className;

		return absClassName;
	}

}
