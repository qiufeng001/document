package com.wayeal.cloud.zipkin;

import org.springframework.util.StringUtils;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka11.KafkaSender;

/**
 * kafka消息组件收集器
 *
 * @author z.h
 */
public class KafkaZipkinCollector extends AbstractZipkinCollector {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String topic;
        private String zipkinUrl;
        private long closeTimeout;
        private int connectionTimeout;
        private String userName;
        private String password;

        public Builder name(String name) {
            if (StringUtils.isEmpty(name)) {
                throw new ZipkinException("name is empty!");
            }
            this.name = name;
            return this;
        }

        public Builder topic(String topic) {
            if (StringUtils.isEmpty(topic)) {
                throw new ZipkinException("topic is empty!");
            }
            this.topic = topic;
            return this;
        }

        public Builder zipkinUrl(String zipkinUrl) {
            if (StringUtils.isEmpty(zipkinUrl)) {
                throw new ZipkinException("zipkinUrl is empty!");
            }
            this.zipkinUrl = zipkinUrl;
            return this;
        }

        public Builder closeTimeout(long closeTimeout) {
            if (StringUtils.isEmpty(closeTimeout)) {
                throw new ZipkinException("closeTimeout is empty!");
            }
            this.closeTimeout = closeTimeout;
            return this;
        }

        public KafkaZipkinCollector builder() {
            return new KafkaZipkinCollector(this);
        }

        public Builder() {
        }
    }

    public KafkaZipkinCollector(Builder builder) {
        super(builder.zipkinUrl, builder.name, builder.topic, builder.closeTimeout,
                builder.connectionTimeout, builder.userName, builder.password);
    }

    @Override
    public Sender getSender(String zipkinUrl, String topic, int connectTimeout, String userName, String password) {
        return KafkaSender
                .newBuilder()
                .bootstrapServers(zipkinUrl)
                .topic(topic)
                .encoding(Encoding.JSON)
                .build();
    }
}
