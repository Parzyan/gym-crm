package com.company.gym.component;

import com.company.gym.dto.request.TrainerWorkloadRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TestJmsListener {

    private final BlockingQueue<TrainerWorkloadRequest> receivedMessages = new LinkedBlockingQueue<>();

    @JmsListener(destination = "${queue.trainer.workload}")
    public void receiveMessage(TrainerWorkloadRequest payload) {
        receivedMessages.add(payload);
    }

    public BlockingQueue<TrainerWorkloadRequest> getReceivedMessages() {
        return receivedMessages;
    }

    public void clear() {
        receivedMessages.clear();
    }
}
