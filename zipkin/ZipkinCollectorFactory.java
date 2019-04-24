package com.wayeal.cloud.zipkin;

import brave.Tracing;

import java.util.Collections;
import java.util.List;


/**
 * 生成zipkin服务的工厂类
 *
 * @author z.h
 */
public class ZipkinCollectorFactory {

    private AbstractZipkinCollector zipkinCollector;
    private String serverName;
    private String topic;
    private String zipkinUrl;
    private long closeTimeout;
    private String senderType;
    private int connectTimeout;
    private String userName;
    private String password;

    public ZipkinCollectorFactory(String serverName, String topic, String zipkinUrl,
                                  long closeTimeout, String senderType, int connectTimeout,
                                  String userName, String password) {
        this.serverName = serverName;
        this.topic = topic;
        this.zipkinUrl = zipkinUrl;
        this.closeTimeout = closeTimeout;
        this.senderType = senderType;
        this.connectTimeout = connectTimeout;
        this.userName = userName;
        this.password = password;
        this.createCollector();
    }

    private void createCollector() {
        if (senderType.equals(TracerConstants.SENTTYPE_KAFKA)) {
            zipkinCollector = KafkaZipkinCollector.newBuilder()
                    .name(serverName).topic(topic).zipkinUrl(zipkinUrl)
                    .closeTimeout(closeTimeout).builder();
        } else if (senderType.equals(TracerConstants.SENTTYPE_HTTP)) {
            zipkinCollector = HttpZipkinCollector.newBuilder()
                    .name(serverName).topic(topic).zipkinUrl(zipkinUrl)
                    .closeTimeout(closeTimeout).builder();
        } else {
            // 自动创建rabbitmq的queue
            TracerUtils.createRabbitMQQueue(zipkinUrl, "/", userName, password, topic);

            // 创建collector
            zipkinCollector = RabbitMQZipkinCollector.newBuilder()
                    .userName(userName)
                    .serviceName(serverName)
                    .password(password)
                    .queue(topic)
                    .addresses(zipkinUrl)
                    .closeTimeout(closeTimeout)
                    .connectionTimeout(connectTimeout)
                    .builder();
        }
    }

    public Tracing getTracing() {
        return this.zipkinCollector.getTracing();
    }
}
