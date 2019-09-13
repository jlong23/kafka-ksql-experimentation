package com.nationwide.nf.ips.streaming.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class BusinessKeyExtractorSettings {
	
	private String seperator;
	private String dynamicKey;
	private Map<String, String> fieldsSet = new HashMap<String, String>();
	
	@SuppressWarnings("deprecation")
	public BusinessKeyExtractorSettings ( String resource ) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        String jsonTxt;
		try {
			jsonTxt = IOUtils.toString( is );
			JSONObject jo = (JSONObject) JSONSerializer.toJSON( jsonTxt );
	        
	        this.seperator = jo.getString("keySeperator");
	        this.dynamicKey = jo.getString("dynamicKey");

	        JSONArray fields = jo.getJSONArray("extractValues");
	        for(int i=0;i<fields.size();i++){
	    		JSONObject item = fields.getJSONObject(i);
	    		this.fieldsSet.put(item.getString("name").trim(), item.getString("xpath"));	    		
	        }
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String getSeperator() {
		return this.seperator;
	}

	public Map<String, String> getFields() {
		// TODO Auto-generated method stub
		return this.fieldsSet;
	}

	public String getDynamicKey() {
		return this.dynamicKey;
	}
	
	public static final void main( String[] args ) {
		BusinessKeyExtractorSettings settings = new BusinessKeyExtractorSettings( args[0] );
	}

}
