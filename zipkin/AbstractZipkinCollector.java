package com.wayeal.cloud.zipkin;

import brave.Tracing;
import brave.sampler.Sampler;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;

import java.util.concurrent.TimeUnit;

/**
 * zipkin 集合配置类
 * <p>
 * 用于初始化Tracing 和 Span
 *
 * @author z.h
 */
public abstract class AbstractZipkinCollector {

    private Tracing tracing;
    private String zipkinUrl;
    private String serviceName;
    private long closeTimeout;
    private int connectTimeout;
    private String topic;
    private String userName;
    private String password;

    public AbstractZipkinCollector(String zipkinUrl, String serviceName, String topic, long closeTimeout,
                                   int connectTimeout, String userName, String password) {
        this.zipkinUrl = zipkinUrl;
        this.serviceName = serviceName;
        this.topic = topic;
        this.closeTimeout = closeTimeout;
        this.connectTimeout = connectTimeout;
        this.userName = userName;
        this.password = password;
        this.tracing = this.tracing();
    }

    public abstract Sender getSender(String zipkinUrl, String topic, int connectTimeout,
                                     String userName, String password);

    /*
     * 初始化Reporter
     */
    public AsyncReporter<Span> spanReporter() {
        return AsyncReporter
                .builder(getSender(zipkinUrl, topic, connectTimeout, userName, password))
                .closeTimeout(closeTimeout, TimeUnit.MILLISECONDS)
                .build(SpanBytesEncoder.JSON_V1);
    }

    /*
     * 初始化Tracing
     */
    public Tracing tracing() {
        return this.tracing = Tracing
                .newBuilder()
                .localServiceName(this.serviceName)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .spanReporter(spanReporter())
                .build();
    }

    public Tracing getTracing() {
        return this.tracing;
    }
}
