package com.example;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.QueueConfig;
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
                .addMember("169.254.198.8:5742")
                .addMember("169.254.198.8:5743")
                .addMember("169.254.198.8:5744")
                .addMember("169.254.198.8:5745")
                .addMember("169.254.198.8:5746")
                .addMember("169.254.198.8:5747");

        config.getJetConfig().setEnabled(true);

        return Hazelcast.newHazelcastInstance(config);
    }
}
