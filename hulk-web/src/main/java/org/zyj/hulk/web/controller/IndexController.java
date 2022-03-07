package org.zyj.hulk.web.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyj
 * @date 2022/1/4 0:13
 */
@Slf4j
@RestController
public class IndexController {

    @Autowired
    private DefaultMQProducer defaultMQProducer;

    private AtomicInteger atomicInteger = new AtomicInteger();

    @PostMapping(value = "/testmq")
    public String testMq() throws UnsupportedEncodingException, MQBrokerException, RemotingException, InterruptedException, MQClientException {
        Message message = new Message("devops-topic", ("testmq" + atomicInteger.getAndIncrement()).getBytes("utf8"));
        SendResult send = defaultMQProducer.send(message);
        log.info("send={}", send);
        return "success";
    }
}
