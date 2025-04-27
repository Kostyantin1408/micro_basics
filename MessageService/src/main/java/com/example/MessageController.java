package com.example;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.orbitz.consul.KeyValueClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.orbitz.consul.Consul;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class MessageController{

    private final HazelcastInstance hazelcastInstance;
    List<String> messages = new ArrayList<>();

    @Autowired
    public MessageController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostConstruct
    public void startConsumer() {
        new Thread(() -> {
            Consul consul = Consul.builder().build();
            KeyValueClient kvClient = consul.keyValueClient();
            Optional<String> queue_name = kvClient.getValueAsString("queue-name");
            IQueue<String> queue;
            queue = queue_name.<IQueue<String>>map(hazelcastInstance::getQueue).orElseGet(() -> hazelcastInstance.getQueue("queue"));
            while (true) {
                try {
                    String message = queue.take();
                    messages.add(message);
                    System.out.println("Consumed message: " + message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Consumer thread was interrupted.");
                    break;
                }
            }
        }).start();
    }

    @GetMapping("/message_service")
    public String messageService(){
        return String.join(", ", messages);
    }
}
