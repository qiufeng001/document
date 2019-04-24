package com.wayeal.cloud.zipkin;

/**
 * zipkip 调用链中静态属性类，用于定义常量
 *
 * @author z.h
 */
public class TracerConstants {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";
    public static final String SENTTYPE_HTTP = "http";
    public static final String SENTTYPE_RABBITMQ = "rabbitMQ";
    public static final String SENTTYPE_KAFKA = "kafka";

    public static final String INTERFACE_NAME = "com.alibaba.dubbo.monitor.MonitorService";
    public static final String INTERFACE_METHOD_NAME = "collect";
}
