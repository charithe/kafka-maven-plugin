/*
 * Copyright 2015 Charith Ellawala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.charithe.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class KafkaMojoTest {
    private static final int ZK_PORT = 52181;
    private static final int KAFKA_PORT = 59092;

    private static final String TOPIC = "topicX";
    private static final String KEY = "keyX";
    private static final String VALUE = "valueX";

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void testBrokerStartup() throws Exception {
        File pom = new File("src/test/resources/unit/project-to-test/pom.xml");
        assertThat(pom, is(notNullValue()));

        Mojo startMojo = rule.lookupMojo("start-kafka-broker", pom);
        assertThat(startMojo, is(notNullValue()));

        Mojo stopMojo = rule.lookupMojo("stop-kafka-broker", pom);
        assertThat(stopMojo, is(notNullValue()));

        startMojo.execute();

        ProducerConfig conf = createProducerConfig();
        Producer<String, String> producer = new Producer<>(conf);
        producer.send(new KeyedMessage<>(TOPIC, KEY, VALUE));
        producer.close();


        ConsumerConfig consumerConf = createConsumerConfig();
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConf);
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(TOPIC, 1);
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer
                .createMessageStreams(topicCountMap, new StringDecoder(consumerConf.props()),
                        new StringDecoder(consumerConf.props()));
        List<KafkaStream<String, String>> streams = consumerMap.get(TOPIC);

        assertThat(streams, CoreMatchers.is(CoreMatchers.notNullValue()));
        assertThat(streams.size(), CoreMatchers.is(equalTo(1)));

        KafkaStream<String, String> ks = streams.get(0);
        ConsumerIterator<String, String> iterator = ks.iterator();
        MessageAndMetadata<String, String> msg = iterator.next();

        assertThat(msg, CoreMatchers.is(CoreMatchers.notNullValue()));
        assertThat(msg.key(), CoreMatchers.is(equalTo(KEY)));
        assertThat(msg.message(), CoreMatchers.is(equalTo(VALUE)));

        stopMojo.execute();
    }


    private ProducerConfig createProducerConfig() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:" + KAFKA_PORT);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("producer.type", "sync");
        props.put("request.required.acks", "1");

        return new ProducerConfig(props);
    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:" + ZK_PORT);
        props.put("group.id", "kafka-maven");
        props.put("zookeeper.session.timeout.ms", "30000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        return new ConsumerConfig(props);
    }
}
