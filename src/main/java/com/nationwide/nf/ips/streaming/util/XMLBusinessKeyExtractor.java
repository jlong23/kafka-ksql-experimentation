package com.nationwide.nf.ips.streaming.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class XMLBusinessKeyExtractor {

	private final Logger logger = LoggerFactory.getLogger(XMLBusinessKeyExtractor.class);

	private String keySeperator = ",";
	private String dynamicKey = "";
	private Map<String, String> extractFields = new HashMap<String, String>();
	private Map<String, String> extractedValues = new HashMap<String, String>();
	
	private XMLUtility xmlUtil = new XMLUtility();
	
	public XMLBusinessKeyExtractor( BusinessKeyExtractorSettings settings ) {
		if( settings != null ) {			
			this.dynamicKey = settings.getDynamicKey();
			this.extractFields = settings.getFields();
			this.keySeperator = settings.getSeperator();
		}
	}
	

	public XMLBusinessKeyExtractor(String keySeperator, Map<String, String> extractFields, String dynamicKeyFormat ) {
		if( keySeperator != null ) {
			this.keySeperator = keySeperator;
		}

		if( extractFields != null ) {
			this.extractFields = extractFields;
		}
		
		if( dynamicKey != null ) {
			this.dynamicKey = dynamicKeyFormat;
			
		}
	}
	
	public void parseXML(Document doc) {
		this.extractedValues =  new HashMap<String, String>();
		
		Set<String> keys = this.extractFields.keySet();
		for( String key : keys ) {
			String value = "";

			try {
				value = xmlUtil.getFirstDocumentElementTextByXPath(doc, this.extractFields.get(key));
			} catch ( Exception e ) {
				logger.error(e.toString());
			}
			
			logger.debug( "Key : " + key);
			logger.debug( "value : " + value);
			
			this.extractedValues.put(key, value);			
		}		
	}
	
	public String getValue( String key ) {
		return this.extractedValues.get(key);
	}
		

	public String getDynamicKey() {
		StringBuilder resultkey = new StringBuilder();
		String[] keyBuilder = this.dynamicKey.split(",");
		
		for( String key : keyBuilder ) {
			if( key.equalsIgnoreCase("KEY_SEPERATOR")) {
				resultkey.append(keySeperator);
			} else {
				if ( this.extractedValues.containsKey(key)) {
					resultkey.append(this.extractedValues.get(key));
				} else {
					logger.error( "Dynamic Key Configuration Error : " + key );
				}
			}
		}

		return resultkey.toString();
	}


	public String getXPath(String key ) {

		return this.extractFields.get(key);
	}
}
