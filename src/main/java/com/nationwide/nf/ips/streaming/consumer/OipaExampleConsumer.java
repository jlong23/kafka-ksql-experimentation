package com.nationwide.nf.ips.streaming.consumer;

import static net.sourceforge.argparse4j.impl.Arguments.store;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class OipaExampleConsumer {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ArgumentParser parser = argParser();

		try {
			Namespace res = parser.parseArgs(args);

			/* parse args */
			String brokerList = res.getString("bootstrap.servers");
			String topic = res.getString("topic");
			String groupId = res.getString("group.id");
			long delay = res.getLong("delay");
			String partition = res.getString("partition");
			String offset = res.getString("seek");

			Properties consumerConfig = new Properties();
			consumerConfig.put("group.id", groupId);
			consumerConfig.put("bootstrap.servers", brokerList);
			consumerConfig.put("auto.offset.reset", "latest");
			consumerConfig.put("max.poll.records", 50 );
			consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.ByteArrayDeserializer");
			consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.ByteArrayDeserializer");
			

			KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(consumerConfig);

			TopicPartition tp = null;
			if( partition != null && !partition.isEmpty()) {
				tp = new TopicPartition( topic, Integer.parseInt( partition ));
				consumer.assign(Collections.singletonList(tp));
				if( offset != null && !offset.isEmpty()) {
					consumer.seek(tp, Long.parseLong(offset));;
				}
			} else {
				consumer.subscribe(Collections.singletonList(topic));
			}

			while (true) {
				ConsumerRecords<byte[], byte[]> records = consumer.poll(1000);
				for (ConsumerRecord<byte[], byte[]> record : records) {
					System.out.printf("Received Message topic =%s, partition =%s, offset = %d, key = %s, value = %s\n",
							record.topic(), record.partition(), record.offset(), deserialize2(record.key()),
							deserialize2(record.value()));
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				consumer.commitSync();
			}

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

	private static String deserialize2(final byte[] objectData) {
		String value = new String(objectData);
		int len = value.length();
		
		return value.substring(0, ( len > 100 ? 100 : len));
	}

	/**
	 * Get the command-line argument parser.
	 */
	private static ArgumentParser argParser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("sweepConsumer").defaultHelp(true)
				.description("This example is to demonstrate kafka consumer capabilities");

		parser.addArgument("--bootstrap.servers").action(store()).required(true).type(String.class)
				.metavar("BROKER-LIST").help("comma separated broker list");

		parser.addArgument("--topic").action(store()).required(true).type(String.class).metavar("TOPIC")
				.help("produce messages to this topic");

		parser.addArgument("--group.id").action(store()).required(true).type(String.class).metavar("GROUPID")
				.help("Consume messages as this group");

		parser.addArgument("--delay").action(store()).required(false).setDefault(10l).type(Long.class).metavar("DELAY")
		.help("number of milli seconds delay between messages.");

		parser.addArgument("--seek").action(store()).required(false).type(String.class).metavar("SEEK")
		.help("Seek to a Partition message offset.");

		parser.addArgument("--partition").action(store()).required(false).type(String.class).metavar("PARTITION")
		.help("Set the Partition to bind.");

		return parser;
	}
}