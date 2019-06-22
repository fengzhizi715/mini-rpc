package com.netdiscovery.minirpc.domain;

import lombok.Data;

/**
 * Created by tony on 2019-06-22.
 */
@Data
public class RpcResponse {

    private String requestId;
    private String error;
    private Object result;
}
