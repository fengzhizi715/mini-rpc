package com.netdiscovery.minirpc.discovery;

/**
 * Created by tony on 2019-06-23.
 */
public interface ServiceDiscovery {

    /**
     * 通过服务发现，获取服务提供方的地址
     * @return
     */
    String discover();
}
