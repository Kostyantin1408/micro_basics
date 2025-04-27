package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        Consul consul = Consul.builder().build();
        KeyValueClient kvClient = consul.keyValueClient();
        Optional<String> logging_server_1 = kvClient.getValueAsString("config/logging-service-1");
        Optional<String> logging_server_2 = kvClient.getValueAsString("config/logging-service-2");
        Optional<String> logging_server_3 = kvClient.getValueAsString("config/logging-service-3");
        Optional<String> facade_server = kvClient.getValueAsString("config/facade-service");
        Optional<String> message_server_1 = kvClient.getValueAsString("config/message-service-1");
        Optional<String> message_server_2 = kvClient.getValueAsString("config/message-service-2");
        config.setInstanceName("logging-service");

        config.setClusterName("logging-cluster");
        QueueConfig queueConfig = config.getQueueConfig("queue");
        queueConfig.setName("queue")
                .setBackupCount(2);

        NetworkConfig networkConfig = config.getNetworkConfig();

        networkConfig.setPort(5742);
        networkConfig.setPortAutoIncrement(true);

        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember(logging_server_1.get())
                .addMember(logging_server_2.get())
                .addMember(logging_server_3.get())
                .addMember(facade_server.get())
                .addMember(message_server_1.get())
                .addMember(message_server_2.get());

        config.getJetConfig().setEnabled(true);

        return Hazelcast.newHazelcastInstance(config);
    }
}
