package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("logging-service");
        config.setClusterName("logging-cluster");

        NetworkConfig networkConfig = config.getNetworkConfig();

        networkConfig.setPort(5742);
        networkConfig.setPortAutoIncrement(true);

        JoinConfig joinConfig = networkConfig.getJoin();

        joinConfig.getMulticastConfig().setEnabled(false);
        config.getJetConfig().setEnabled(true);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("10.10.225.93:5742")
                .addMember("10.10.225.93:5743")
                .addMember("10.10.225.93:5744");

        return Hazelcast.newHazelcastInstance(config);
    }
}
