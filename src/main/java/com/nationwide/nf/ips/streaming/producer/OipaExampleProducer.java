package com.nationwide.nf.ips.streaming.producer;

import static net.sourceforge.argparse4j.impl.Arguments.store;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.nationwide.nf.ips.streaming.util.BusinessKeyExtractorSettings;
import com.nationwide.nf.ips.streaming.util.XMLBusinessKeyExtractor;
import com.nationwide.nf.ips.streaming.util.XMLUtility;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class OipaExampleProducer {

	public static final String XML_TEST_FILE = "AnnualStatementTest.xml";
	private final Logger logger = LoggerFactory.getLogger(OipaExampleProducer.class);
	private final Date effectiveDate = new Date();
	private XMLUtility xmlUtil = new XMLUtility();
	private XMLBusinessKeyExtractor extractor;

	private String xmlTestFile = XML_TEST_FILE;

	public static void main(String[] args) {
		OipaExampleProducer prod = new OipaExampleProducer();
		prod.producer(args);
	}

	public void producer(String[] args) {

		ArgumentParser parser = argParser();

		try {
			Namespace res = parser.parseArgs(args);

			/* parse args */
			String brokerList = res.getString("bootstrap.servers");
			String topic = res.getString("topic");
			long noOfMessages = res.getLong("messages");
			long delay = res.getLong("delay");
			
			this.xmlTestFile = res.getString("file");
			String keyExtractrSettings = res.getString("extractor");

			BusinessKeyExtractorSettings settings = new BusinessKeyExtractorSettings( keyExtractrSettings );
			this.extractor = new XMLBusinessKeyExtractor( settings );
			
			
			Properties producerConfig = new Properties();
			producerConfig.put("bootstrap.servers", brokerList);
			producerConfig.put("client.id", "oipa-producer");
			producerConfig.put("acks", "all");
			producerConfig.put("retries", "3");
			producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.ByteArraySerializer");
			producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.ByteArraySerializer");

			SimpleProducer producer = new SimpleProducer(producerConfig, Boolean.TRUE);

			Document sourceDoc = xmlUtil.getTestFile(xmlTestFile);

			// For each test document 
			for (int i = 0; i < noOfMessages; i++) {
				
				UUID uuid = UUID.randomUUID();
				Document sampleDoc = createSampleDocument(sourceDoc, i, uuid);				
				extractor.parseXML(sampleDoc);				
				String sampleKey = uuid.toString();

				logger.info("Sending Key: {}", sampleKey);

				producer.send(topic, xmlUtil.serialize(sampleKey), xmlUtil.serialize(sampleDoc));
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			producer.close();
		} catch (ArgumentParserException e) {
			if (args.length == 0) {
				parser.printHelp();
				System.exit(0);
			} else {
				parser.handleError(e);
				System.exit(1);
			}
		}

	}

	private Document createSampleDocument(Document sourceDoc, int index, UUID uuid ) {

		Document resultDoc = null;
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmSS");

		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();
			Transformer tx = tfactory.newTransformer();
			DOMSource source = new DOMSource(sourceDoc);
			DOMResult result = new DOMResult();

			// Make a copy of the Original Document
			tx.transform(source, result);
			resultDoc = (Document) result.getNode();

			// Tweek Values via XPATH

			// Set the Header Message ID
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "MESSAGE_ID" ),
					uuid.toString());

			// Set the Activity ID
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "ACTIVITY_GUID" ),
					uuid.toString());

			// Update the Effective Date
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "EFFECTIVE_DATE" ),
					sdf.format(effectiveDate));

			String fakePolicyID = Integer.toString(80000000 + index);

			// Set the Policy ID
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "POLICYNUMBER" ),
					fakePolicyID);

			// Update the State Codes
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "MAILING_STATECODE" ),
					Integer.toString( getRandomNumberInRange( 1,50 )));

			// Update the Product Code
			xmlUtil.updateDocumentElementTextByXPath(resultDoc, this.extractor.getXPath( "PRODUCT_ID" ),
					Integer.toString( getRandomNumberInRange( 1,3 )));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultDoc;
	}
	
	private int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		return (int)(Math.random() * ((max - min) + 1)) + min;
	}


	/**
	 * Get the command-line argument parser.
	 */
	private static ArgumentParser argParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("streamProducer").defaultHelp(true)
				.description("This example is to demonstrate kafka producer capabilities");

		parser.addArgument("--bootstrap.servers").action(store()).required(true).type(String.class)
				.metavar("BROKER-LIST").help("comma separated broker list");

		parser.addArgument("--file").action(store()).required(true).type(String.class).metavar("FILE")
		.help("Source file resource to use");

		parser.addArgument("--topic").action(store()).required(true).type(String.class).metavar("TOPIC")
				.help("produce messages to this topic");

		parser.addArgument("--messages").action(store()).required(true).type(Long.class).metavar("NUM-MESSAGE")
				.help("number of messages to produce");

		parser.addArgument("--delay").action(store()).required(false).setDefault(10l).type(Long.class).metavar("DELAY")
				.help("number of milli seconds delay between messages.");

		parser.addArgument("--extractor").action(store()).setDefault("extractor.json").type(String.class).metavar("EXTRACTOR")
		.help("JSON Configuration settings for value extraction & key generation");

		return parser;
	}

}