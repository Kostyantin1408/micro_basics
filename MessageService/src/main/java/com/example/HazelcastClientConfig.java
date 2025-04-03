package com.example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastClientConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("logging-cluster");
        clientConfig.getNetworkConfig()
                .addAddress("169.254.198.8:5742",
                        "169.254.198.8:5743",
                        "169.254.198.8:5744",
                        "169.254.198.8:5745",
                        "169.254.198.8:5746",
                        "169.254.198.8:5747");
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
