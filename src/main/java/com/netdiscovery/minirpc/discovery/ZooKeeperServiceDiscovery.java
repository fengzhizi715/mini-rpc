package com.netdiscovery.minirpc.discovery;

import com.netdiscovery.minirpc.client.ConnectManager;
import com.netdiscovery.minirpc.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by tony on 2019-06-26.
 */
@Slf4j
public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> serviceAddressList = new ArrayList<>();

    private String registryAddress; // 注册中心的地址

    public ZooKeeperServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    @Override
    public String discover() {
        String data = null;
        int size = serviceAddressList.size();
        if (size > 0) {
            if (size == 1) {  //只有一个服务提供方
                data = serviceAddressList.get(0);
                log.info("unique service address : {}", data);
            } else {          //使用随机分配法。简单的负载均衡法
                data = serviceAddressList.get(ThreadLocalRandom.current().nextInt(size));
                log.info("choose an address : {}", data);
            }
        }
        return data;
    }


    /**
     * 连接 zookeeper
     * @return
     */
    private ZooKeeper connectServer() {

        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            log.error("", e);
        }
        return zk;
    }

    /**
     * 获取服务地址列表
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {

        try {
            //获取子节点列表
            List<String> nodeList = zk.getChildren(Constants.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        //发生子节点变化时再次调用此方法更新服务地址
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constants.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            log.debug("node data: {}", dataList);
            this.serviceAddressList = dataList;
            ConnectManager.getInstance().updateConnectedServer(this.serviceAddressList);
        } catch (KeeperException | InterruptedException e) {
            log.error("", e);
        }
    }
}
