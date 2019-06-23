package com.netdiscovery.minirpc.client;

/**
 * Created by tony on 2019-06-23.
 */
public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);
}
