package com.nationwide.nf.ips.streaming.processor;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.nationwide.nf.ips.streaming.util.BusinessKeyExtractorSettings;
import com.nationwide.nf.ips.streaming.util.XMLBusinessKeyExtractor;
import com.nationwide.nf.ips.streaming.util.XMLUtility;

public class KeyStreamTransformer implements Processor<String, String>, Punctuator {

	private final Logger logger = LoggerFactory.getLogger(KeyStreamTransformer.class);
	private final XMLUtility xmlUtil = new XMLUtility();
	private XMLBusinessKeyExtractor extractor;

	private ProcessorContext context;
	
	public KeyStreamTransformer( BusinessKeyExtractorSettings settings ) {
		this.extractor = new XMLBusinessKeyExtractor(settings.getSeperator(), settings.getFields(), settings.getDynamicKey() );
	}
	
	
	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and
		// commit()
		this.context = context;
	}

	/**
	 * For each message in the listened topic, 
	 * deep inspect the message; pulling out the business 
	 * Key values needed for KSQL Cross Cutting. 
	 * 
	 * @param key Source Message Key field
	 * @param value Source Message value (AsXML)
	 */
	public void process(String key, String value) {
		String dynamicKey = "";
		
		if( value != null && key != null ) {
			Document doc = xmlUtil.deSerialize(value.getBytes());
			
			// Try to get the needed values
			extractor.parseXML(doc);
			
			dynamicKey = extractor.getDynamicKey();
			
			
			StringBuilder ksqlKey = new StringBuilder();
			ksqlKey.append(context.partition()).append(",");
			ksqlKey.append(context.offset()).append(",");
			ksqlKey.append(dynamicKey);
			
			logger.debug(ksqlKey.toString());

			context.forward(key, ksqlKey.toString() );			
		}
	}

	@Override
	public void punctuate(long timestamp) {
		// I'd put metrics here,
		//    IF I HAD SOME!!
		//    	https://imgflip.com/memegenerator/This-Is-Where-Id-Put-My-Trophy-If-I-Had-One
		
	}

	@Override
	public void close() {
		// close any resources managed by this processor.
		// Note: Do not close any StateStores as these are managed
		// by the library
	}

}
