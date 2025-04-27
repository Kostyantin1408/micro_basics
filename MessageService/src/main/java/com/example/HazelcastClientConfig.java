package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class HazelcastClientConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("logging-cluster");

        Config config = new Config();
        Consul consul = Consul.builder().build();
        KeyValueClient kvClient = consul.keyValueClient();
        Optional<String> logging_server_1 = kvClient.getValueAsString("config/logging-service-1");
        Optional<String> logging_server_2 = kvClient.getValueAsString("config/logging-service-2");
        Optional<String> logging_server_3 = kvClient.getValueAsString("config/logging-service-3");
        Optional<String> facade_server = kvClient.getValueAsString("config/facade-service");
        Optional<String> message_server_1 = kvClient.getValueAsString("config/message-service-1");
        Optional<String> message_server_2 = kvClient.getValueAsString("config/message-service-2");

        clientConfig.getNetworkConfig()
                .addAddress(logging_server_1.get(),
                        logging_server_2.get(),
                        logging_server_3.get(),
                        facade_server.get(),
                        message_server_1.get(),
                        message_server_2.get());
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
