package com.nationwide.nf.ips.streaming.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtility {
	private final Logger logger = LoggerFactory.getLogger(XMLUtility.class);

	public Document getTestFile(String resourcePath) {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;

		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// by sending input stream as input to DOM
			doc = docBuilder.parse(in);
		} catch (ParserConfigurationException e) {
			logger.error("XML Parse Exception", e);
		} catch (SAXException e) {
			logger.error("XML Parse Exception", e);
		} catch (IOException e) {
			logger.error("IO Exception", e);
		}

		return doc;
	}

	public void updateDocumentElementTextByXPath(Document doc, String xpathString, String value) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList myNodeList = (NodeList) xpath.compile(xpathString).evaluate(doc, XPathConstants.NODESET);

			for (int idx = 0; idx < myNodeList.getLength(); idx++) {
				myNodeList.item(idx).setTextContent(value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFirstDocumentElementTextByXPath(Document doc, String xpathString) {
		String value = new String();

		try {
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList myNodeList = (NodeList) xpath.compile(xpathString).evaluate(doc, XPathConstants.NODESET);

			value = myNodeList.item(0).getTextContent();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	public byte[] serialize(final String obj) {
		return obj.getBytes();
	}

	public byte[] serialize(final Document obj) {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Transformer transformer;
		try {
			transformer = tfactory.newTransformer();
			DOMSource source = new DOMSource(obj);
			StreamResult result = new StreamResult(bos);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bos.toByteArray();
	}

	public Document deSerialize(byte[] resourceBytes) {
		InputStream in = new ByteArrayInputStream(resourceBytes);
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;

		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// by sending input stream as input to DOM
			doc = docBuilder.parse(in);
		} catch (ParserConfigurationException e) {
			logger.error("XML Parse Exception", e);
		} catch (SAXException e) {
			logger.error("XML Parse Exception", e);
		} catch (IOException e) {
			logger.error("IO Exception", e);
		}

		return doc;
	}

}
