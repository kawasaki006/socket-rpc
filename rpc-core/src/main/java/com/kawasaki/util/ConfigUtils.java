package com.kawasaki.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.setting.dialect.Props;
import com.kawasaki.config.RpcConfig;

public class ConfigUtils {
    private static final String CONFIG_FILE_NAME = "rpc-config.properties";
    private static RpcConfig rpcConfig;

    private ConfigUtils() {}

    private static void loadConfig() {
        if (ResourceUtil.getResource(CONFIG_FILE_NAME) == null) {
            rpcConfig = new RpcConfig();
            return;
        }

        Props props = new Props(CONFIG_FILE_NAME);
        if (props.isEmpty()) {
            rpcConfig = new RpcConfig();
            return;
        }

        rpcConfig = props.toBean(RpcConfig.class);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            loadConfig();
        }

        return rpcConfig;
    }
}
