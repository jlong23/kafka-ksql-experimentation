package com.nationwide.nf.ips.streaming.processor;

import static net.sourceforge.argparse4j.impl.Arguments.store;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nationwide.nf.ips.streaming.util.BusinessKeyExtractorSettings;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class StreamProcessor {
	private static final String APPLICATION_ID_SUFFIX = "-processor";
	private static final String OUTPUT_TOPIC_SUFFIX = ".key-stream";

	private final Logger logger = LoggerFactory.getLogger(StreamProcessor.class);

	public static void main(String[] args) {
		StreamProcessor proc = new StreamProcessor();
		proc.processor(args);
	}

	/**
	 * 
	 * @param args
	 */
	public void processor(String[] args) {

		ArgumentParser parser = argParser();

		try {
			Namespace res = parser.parseArgs(args);

			/* parse args */
			String brokerList = res.getString("bootstrap.servers");
			String topicBase = res.getString("topic");

			String keyExtractrSettings = res.getString("extractor");

			// Routing Settings
			String routingConfig = res.getString("config");
			Map<String, List<String>> route = loadRouting(routingConfig);
			
			Properties props = new Properties();
			logger.info("ApplicationID: " + topicBase + APPLICATION_ID_SUFFIX);
			props.put(StreamsConfig.APPLICATION_ID_CONFIG, topicBase + APPLICATION_ID_SUFFIX);
			props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
			props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

			props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
			
			final StreamsBuilder builder = new StreamsBuilder();

			logger.info("InputTopic: " + topicBase);
			logger.info("OutputTopic: " + topicBase + OUTPUT_TOPIC_SUFFIX);

			final Topology topology = builder.build();
			final CountDownLatch latch = new CountDownLatch(1);
			
			
			BusinessKeyExtractorSettings settings = new BusinessKeyExtractorSettings( keyExtractrSettings );
			

			// add the source processor node that takes Kafka topic "source-topic" as input
			topology.addSource("Source", topicBase)

				// add the WordCountProcessor node which takes the source processor as its
				// upstream processor
				.addProcessor("ProcessIndex", () -> new KeyStreamTransformer( settings ), "Source")
				.addSink("SinkIndex", topicBase + OUTPUT_TOPIC_SUFFIX, "ProcessIndex");

			// If there is a Routing Content/
			if (!route.isEmpty()) {
				int index = 1;

				for (String outputTopic : route.keySet()) {
					String instance = String.valueOf(index);
					
					logger.info("OutputTopic: " + outputTopic + " : " +  route.get(outputTopic));


					// add the source processor node that takes Kafka topic "source-topic" as input
					topology.addProcessor("ProcessL" + instance, () -> new StreamCopier(outputTopic, route.get(outputTopic)), "Source")
						.addSink("Sink" + instance, outputTopic, "ProcessL" + instance);
					
					index ++;
				}
			}

			final KafkaStreams streams = new KafkaStreams(topology, props);

			// attach shutdown handler to catch control-c
			Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
				@Override
				public void run() {
					logger.warn("Shutdown Requested");
					streams.close();
					latch.countDown();
				}
			});

			streams.start();

			latch.await();
		} catch (Throwable e) {
			logger.error("Exception Caught, Quiting.", e);
			System.exit(1);
		}
		logger.error("Pipe-Ending.");
		System.exit(0);
	}

	/**
	 * Get the command-line argument parser.
	 */
	private static ArgumentParser argParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("streamProcessor").defaultHelp(true)
				.description("This example is to demonstrate process & transform capabilities");

		parser.addArgument("--topic").action(store()).required(true).type(String.class).metavar("TOPIC")
		.help("Process messages in this topic");

		parser.addArgument("--config").action(store()).setDefault("processor.properties").type(String.class).metavar("CONFIG")
		.help("Configuration settings for Routing");

		parser.addArgument("--extractor").action(store()).setDefault("extractor.json").type(String.class).metavar("EXTRACTOR")
		.help("JSON Configuration settings for value extraction & key generation");

		parser.addArgument("--bootstrap.servers").action(store()).required(true).type(String.class)
				.metavar("BROKER-LIST").help("comma separated broker list");

		return parser;
	}
	
	private Map<String,List<String>> loadRouting( String propFileName ) {
		Map<String, List<String>> routes = new HashMap<String, List<String>>();
		
		Properties routeProperties = new Properties();

		InputStream inputStream = null;
		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			 
			if (inputStream != null) {
				routeProperties.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			
			
			for ( Enumeration<?> keyEnum = routeProperties.propertyNames(); keyEnum.hasMoreElements(); ) {
				String key = (String) keyEnum.nextElement();
				String[] keySet = key.split("\\.");

				if( keySet.length == 3 && keySet[0].toLowerCase().equals("route") && keySet[1].toLowerCase().equals("topic") ) {

					List<String> messageTypes = new ArrayList<String>();
					
					String topicName = routeProperties.getProperty( "route.topic." + keySet[2] );
					String messageKeys = routeProperties.getProperty( "route.types." + keySet[2] );

					logger.info( "route.topic." + keySet[2] + " = " + topicName );

					
					String[] messageSet = messageKeys.split(".");
					for ( String msgType : messageSet ) {
						messageTypes.add(msgType);
					}					

					routes.put(topicName, messageTypes);
				}				
			}										
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
		
		return routes;
	}


}
