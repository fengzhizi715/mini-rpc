package com.netdiscovery.minirpc.domain;

import lombok.Data;

/**
 * Created by tony on 2019-06-22.
 */
@Data
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
