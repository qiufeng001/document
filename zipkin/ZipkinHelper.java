package com.wayeal.cloud.zipkin;

import brave.Span;
import brave.Tracer;
import brave.internal.Platform;
import brave.propagation.TraceContext;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import zipkin2.internal.HexCodec;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 帮助类，用于对zipkin相关处理做分离
 *
 * @author z.h
 */
public class ZipkinHelper implements Zipkin {

    @Override
    public Result excute(Invoker<?> invoker, Invocation invocation) {

        RpcContext rpcContext = RpcContext.getContext();
        boolean whetherMonitor = invoker.getInterface().getName().equals(TracerConstants.INTERFACE_NAME)
                && rpcContext.getMethodName().equals(TracerConstants.INTERFACE_METHOD_NAME);
        if (whetherMonitor) {
            return invoker.invoke(invocation);
        }

        // 是否开启追踪
        boolean enableTrace = Boolean.valueOf(PropertiesUtils.getDubboProp().getProperty("zipkin.tracing.enableTrace"));
        if(!enableTrace) {
            return invoker.invoke(invocation);
        }

        Tracer tracer = SpringContextUtils.getApplicationContext()
                .getBean(ZipkinCollectorFactory.class).getTracing().tracer();
        // 判断是否存在traceId或者判断traceId存在后给rpcContext添加traceId和spanId
        boolean flag = this.isTraceIdIsEmpty(rpcContext, tracer);
        if (!flag) {
            return this.tracing(tracer, invoker, invocation, rpcContext);
        }
        return invoker.invoke(invocation);
    }

    /**
     * @param rpcContext dubbo 上下文传递对象
     * @param tracer     zipkin 调用链对象
     * @return 判断上下文中是否存在traceId
     */
     private boolean isTraceIdIsEmpty(RpcContext rpcContext, Tracer tracer) {
        boolean flag = false;
        Map<String, String> attachmentMap = rpcContext.getAttachments();

        /*
         *  判断是第一次请求的还是经过多个请求之后，attachments中是否存在traceId
         *  如果请求中没有traceId，则跳过，即不执行调用链
         *  如果是经过几次调用之后，trace对象中traceId存在，但是rpcContext对象中不存在，则在上下文中添加上
         */
        String traceId = attachmentMap.get("traceId");
        if (StringUtils.isEmpty(traceId) || attachmentMap.size() <= 0) {
            if (tracer.currentSpan() != null) {
                long contextTraceId = tracer.currentSpan().context().traceId();
                if (!org.springframework.util.StringUtils.isEmpty(contextTraceId)) {
                    rpcContext.setAttachment(TracerConstants.TRACE_ID_KEY, String.valueOf(contextTraceId));
                    rpcContext.setAttachment(TracerConstants.SPAN_ID_KEY, String.valueOf(tracer.currentSpan().context().spanId()));
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 执行上下文
     *
     * @param tracer     tace 对象
     * @param invoker
     * @param invocation dubbo远程调用
     * @param rpcContext dubbo上下文
     * @return 服务运行结果
     */
    private Result tracing(Tracer tracer, Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        return spanTracing(buildSpan(tracer, invoker, invocation, rpcContext), tracer, invoker, invocation, rpcContext);
    }

    /**
     * 创建span
     *
     * @param tracer     调用链对象
     * @param invoker
     * @param invocation dubbo远程调用
     * @param rpcContext dubbo上下文
     * @return span对象
     */
    private static Span buildSpan(Tracer tracer, Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        return toSpan(tracer, invoker, invocation, rpcContext);
    }

    /**
     * 创建span对象
     *
     * @return
     */
    private static Span toSpan(Tracer tracer, Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        // 组织traceContext 上下文对象
        return toBuilderSpan(tracer, invoker, invocation, rpcContext, toTraceContext(rpcContext));
    }

    /**
     * 组织TraceContext上下文
     *
     * @param rpcContext dubbo 上下文
     * @return traceContext 上下问对象
     */
    private static TraceContext toTraceContext(RpcContext rpcContext) {
        TraceContext traceContext = null;
        Map<String, String> attaches = rpcContext.getAttachments();
        if (attaches.containsKey(TracerConstants.TRACE_ID_KEY)) {
            if (attaches.containsKey(TracerConstants.SPAN_ID_KEY)) {
                long traceId = Long.valueOf(attaches.get(TracerConstants.TRACE_ID_KEY));
                Long spanId = Long.valueOf(attaches.get(TracerConstants.SPAN_ID_KEY));

                traceContext = TraceContext.newBuilder()
                        .traceId(traceId)
                        .parentId(spanId)
                        .spanId(TracerUtils.get())
                        .sampled(true)
                        .build();
            } else {
                String id = attaches.get(TracerConstants.TRACE_ID_KEY);
                if (StringUtils.isNotEmpty(id)) {
                    long traceId = HexCodec.lowerHexToUnsignedLong(id);
                    traceContext = TraceContext.newBuilder()
                            .traceId(traceId)
                            .parentId(traceId)
                            .spanId(TracerUtils.get())
                            .sampled(true)
                            .build();
                }
            }
        }
        return traceContext;
    }

    /**
     * 组织span
     *
     * @return
     */
    private static Span toBuilderSpan(Tracer tracer, Invoker<?> invoker, Invocation invocation, RpcContext rpcContext, TraceContext traceContext) {
        String methodName = rpcContext.getMethodName();

        Span span = tracer.toSpan(traceContext).start();
        span.remoteServiceName(invoker.getInterface().getSimpleName());
        span.annotate("server strat");
        span.name(RpcUtils.getMethodName(invocation));
        InetSocketAddress remoteAddress = rpcContext.getRemoteAddress();
        span.remoteIpAndPort(Platform.get().getHostString(remoteAddress), remoteAddress.getPort());
        span.tag("http.path", invoker.getInterface().getSimpleName() + "/" + methodName);

        boolean isCS = rpcContext.isConsumerSide();
        if (!isCS) {
            span.tag("server type", "provider");
        } else {
            span.tag("server type", "consumer");
        }

        rpcContext.setAttachment(TracerConstants.TRACE_ID_KEY, String.valueOf(span.context().traceId()));
        rpcContext.setAttachment(TracerConstants.SPAN_ID_KEY, String.valueOf(span.context().spanId()));
        return span;
    }

    private static Result spanTracing(Span span, Tracer tracer, Invoker<?> invoker, Invocation invocation, RpcContext rpcContext) {
        boolean isOneway = false;
        boolean deferFinish = false;
        try (Tracer.SpanInScope scope = tracer.withSpanInScope(span)) {
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                onError(result.getException(), span);
            }
            isOneway = RpcUtils.isOneway(invoker.getUrl(), invocation);
            Future<Object> future = rpcContext.getFuture(); // the case on async client invocation
            if (future instanceof FutureAdapter) {
                deferFinish = true;
                ((FutureAdapter) future).getFuture().setCallback(new FinishSpanCallback(span));
            }
            return result;
        } catch (Exception e) {
            onError(e, span);
            throw e;
        } finally {
            if (isOneway) {
                span.flush();
            } else if (!deferFinish) {
                span.annotate("server finish");
                span.finish();
            }
        }
    }

    private static void onError(Throwable error, Span span) {
        span.error(error);
        if (error instanceof RpcException) {
            span.tag("dubbo.error_code", Integer.toString(((RpcException) error).getCode()));
        }
    }

    private static final class FinishSpanCallback implements ResponseCallback {
        final Span span;

        FinishSpanCallback(Span span) {
            this.span = span;
        }

        @Override
        public void done(Object response) {
            span.finish();
        }

        @Override
        public void caught(Throwable exception) {
            onError(exception, span);
            span.finish();
        }
    }
}