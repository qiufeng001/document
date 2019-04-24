package com.wayeal.cloud.zipkin;

import org.springframework.util.StringUtils;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;
import zipkin2.reporter.amqp.RabbitMQSender;

/**
 * RabbitMQ消息组件收集器
 *
 * @author z.h
 */
public class RabbitMQZipkinCollector extends AbstractZipkinCollector {

    public static Builder newBuilder() {
        return new RabbitMQZipkinCollector.Builder();
    }

    public static class Builder {
        private String serviceName;
        private String topic;
        private String zipkinUrl;
        private long closeTimeout;
        private int connectionTimeout;
        private String userName;
        private String password;

        public Builder serviceName(String serviceName) {
            if(StringUtils.isEmpty(serviceName)) { throw new ZipkinException("serviceName is empty!");}
            this.serviceName  = serviceName;
            return this;
        }

        public Builder closeTimeout(long closeTimeout) {
            if(StringUtils.isEmpty(closeTimeout)) { throw new ZipkinException("closeTimeout is empty!");}
            this.closeTimeout  = closeTimeout;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            if(StringUtils.isEmpty(connectionTimeout)) { throw new ZipkinException("connectionTimeout is empty!");}
            this.connectionTimeout  = connectionTimeout;
            return this;
        }

        public Builder queue(String queue) {
            if(StringUtils.isEmpty(queue)) { throw new ZipkinException("queue is empty!");}
            this.topic  = queue;
            return this;
        }

        public Builder addresses(String zipkinUrl) {
            if(StringUtils.isEmpty(zipkinUrl)) { throw new ZipkinException("zipkinUrl is empty!");}
            this.zipkinUrl  = zipkinUrl;
            return this;
        }

        public Builder userName(String userName) {
            if(StringUtils.isEmpty(userName)) { throw new ZipkinException("userName is empty!");}
            this.userName  = userName;
            return this;
        }

        public Builder password(String password) {
            if(StringUtils.isEmpty(password)) { throw new ZipkinException("password is empty!");}
            this.password  = password;
            return this;
        }

        public RabbitMQZipkinCollector builder(){
            return new RabbitMQZipkinCollector(this);
        }

        public Builder() {}
    }

    public RabbitMQZipkinCollector(Builder builder) {
        super(builder.zipkinUrl, builder.serviceName, builder.topic,
                builder.closeTimeout, builder.connectionTimeout,
                builder.userName, builder.password);
    }

    @Override
    public Sender getSender(String zipkinUrl, String queue, int connectTimeout, String userName, String password) {
        return RabbitMQSender.newBuilder()
                .addresses(zipkinUrl)
                .queue(queue)
                .encoding(Encoding.JSON)
                .connectionTimeout(connectTimeout)
                .username(userName)
                .password(password)
                .build();
    }
}
