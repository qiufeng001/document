package com.wayeal.cloud.zipkin;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.rpc.*;


/**
 * zipkin 过滤器
 *
 * @author z.h
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class ZipkinTracerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return ExtensionLoader.getExtensionLoader(Zipkin.class)
                .getExtension("zipkinHelper").excute(invoker, invocation);
    }
}

