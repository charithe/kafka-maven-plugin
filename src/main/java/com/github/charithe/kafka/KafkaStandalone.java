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

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.curator.test.TestingServer;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

public enum KafkaStandalone {

    INSTANCE;

    private TestingServer zookeeper;
    private KafkaServerStartable kafkaServer;
    private Path kafkaLogDir;


    public void start(int zookeeperPort, int kafkaPort) throws Exception {
        zookeeper = new TestingServer(zookeeperPort, true);
        String zookeeperConnectionString = zookeeper.getConnectString();
        KafkaConfig kafkaConfig = buildKafkaConfig(zookeeperConnectionString, kafkaPort);
        kafkaServer = new KafkaServerStartable(kafkaConfig);
        kafkaServer.startup();
    }

    private KafkaConfig buildKafkaConfig(String zookeeperQuorum, int kafkaPort) throws IOException {
        kafkaLogDir = Files.createTempDirectory("kafka_maven");

        Properties props = new Properties();
        props.put("port", kafkaPort + "");
        props.put("broker.id", "1");
        props.put("log.dirs", kafkaLogDir.toAbsolutePath().toString());
        props.put("zookeeper.connect", zookeeperQuorum);

        return new KafkaConfig(props);
    }

    public void stop() {
        try {
            if (kafkaServer != null) {
                kafkaServer.shutdown();
            }

            if (zookeeper != null) {
                zookeeper.close();
            }

            if (Files.exists(kafkaLogDir)) {
                Files.walkFileTree(kafkaLogDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
