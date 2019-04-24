package com.wayeal.cloud.zipkin;

import org.springframework.util.StringUtils;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * 通过http形式向zipkin服务器中添加数据
 * 获取发送数据对象，（数据通道）
 *
 * @author z.h
 */
public class HttpZipkinCollector extends AbstractZipkinCollector {

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
            if(StringUtils.isEmpty(name)) { throw new ZipkinException("name is empty!");}
            this.name  = name;
            return this;
        }

        public Builder topic(String topic) {
            if(StringUtils.isEmpty(topic)) { throw new ZipkinException("topic is empty!");}
            this.topic  = topic;
            return this;
        }

        public Builder zipkinUrl(String zipkinUrl) {
            if(StringUtils.isEmpty(zipkinUrl)) { throw new ZipkinException("zipkinUrl is empty!");}
            this.zipkinUrl  = zipkinUrl;
            return this;
        }

        public Builder closeTimeout(long closeTimeout) {
            if(StringUtils.isEmpty(closeTimeout)) { throw new ZipkinException("closeTimeout is empty!");}
            this.closeTimeout  = closeTimeout;
            return this;
        }

        public HttpZipkinCollector builder() {
            return new HttpZipkinCollector(this);
        }

        public Builder() {}
    }

    public HttpZipkinCollector(Builder builder) {
        super(builder.zipkinUrl, builder.name, builder.topic, builder.closeTimeout,
                builder.connectionTimeout, builder.userName, builder.password);
    }

    @Override
    public Sender getSender(String zipkinUrl, String topic, int connectTimeout, String userName, String password) {
        return OkHttpSender.create(zipkinUrl);
    }
}
