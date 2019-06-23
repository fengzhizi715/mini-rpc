package com.netdiscovery.minirpc.client;

import com.netdiscovery.minirpc.domain.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by tony on 2019-06-23.
 */
@Slf4j
public class RPCProxy<T> implements InvocationHandler {

    private Class<T> clazz;

    public RPCProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        // Debug
        log.debug(method.getDeclaringClass().getName());
        log.debug(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            log.debug(method.getParameterTypes()[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            log.debug(args[i].toString());
        }

        RpcClientHandler handler = ConnectManager.getInstance().chooseHandler();
        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }
}
