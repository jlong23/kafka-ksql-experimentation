package com.nationwide.nf.ips.streaming.processor;

import java.util.List;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.nationwide.nf.ips.streaming.util.XMLUtility;

public class StreamCopier implements Processor<String, String>, Punctuator {

	private final Logger logger = LoggerFactory.getLogger(StreamCopier.class);

	public static final String XPATH_TRANSACTION_NAME = "//messageHeader/TransactionName/text()";
		
	private final XMLUtility xmlUtil = new XMLUtility();

	private ProcessorContext context;
	private String routedTopic;
	private List<String> transactionList;
	
	public StreamCopier(String routedTopic, List<String> transactions ) {
		this.transactionList = transactions;
	}
	
	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and
		// commit()
		this.context = context;
	}

	/**
	 * For each message in the listened topic, 
	 * Copy the message unaltered. 
	 * 
	 * @param key Source Message Key field
	 * @param value Source Message value (AsXML)
	 */
	public void process(String key, String value) {
		
		if( value != null && key != null ) {
			Document doc = xmlUtil.deSerialize(value.getBytes());
			
			String transactionName = xmlUtil.getFirstDocumentElementTextByXPath(doc, XPATH_TRANSACTION_NAME);
			if( this.transactionList.contains(transactionName) || this.transactionList.contains( "*" )) {
				logger.debug("Message : " + key.toString() + ", Topic : " + routedTopic );
				context.forward(key, value );			
				
			}
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
