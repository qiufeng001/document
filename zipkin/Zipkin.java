package com.wayeal.cloud.zipkin;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;

@SPI
public interface Zipkin {
    Result excute(Invoker<?> invoker, Invocation invocation);
}
