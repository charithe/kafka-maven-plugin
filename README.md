Kafka Maven Plugin
===================

A Maven plugin to start a Kafka broker during integration tests


Usage
------

Use in conjunction with the failsafe plugin to start the broker before integration tests and tear it down afterwards. 

```xml
<build>
    <plugins>
       <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>2.14.1</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>com.github.charithe</groupId>
            <artifactId>kafka-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <configuration>
                <zookeeperPort>52181</zookeeperPort>
                <kafkaPort>59092</kafkaPort>
            </configuration>
            <executions>
                <execution>
                    <id>preintegration</id>
                    <phase>pre-integration-test</phase>
                    <goals>
                        <goal>start-kafka-broker</goal>
                    </goals>
                </execution>
                <execution>
                    <id>postintegration</id>
                    <phase>post-integration-test</phase>
                    <goals>
                        <goal>stop-kafka-broker</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```