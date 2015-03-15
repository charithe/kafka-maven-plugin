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


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo (name = "start-kafka-broker")
public class StartKafkaBrokerMojo extends AbstractMojo {

    @Parameter (defaultValue = "9092")
    private Integer kafkaPort;

    @Parameter (defaultValue = "2181")
    private Integer zookeeperPort;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Starting Zookeeper on port " + zookeeperPort + " and Kafka broker on port " + kafkaPort);
            KafkaStandalone.INSTANCE.start(zookeeperPort, kafkaPort);
        } catch (Exception e) {
            getLog().error("Failed to start Kafka broker", e);
            throw new MojoExecutionException("Failed to start Kafka broker");
        }
    }
}
