package com.kawasaki.util;

import cn.hutool.core.util.StrUtil;

import java.net.InetSocketAddress;
import java.util.Objects;

public class IpUtils {
    public static String ToIpPort(InetSocketAddress address) {
        if (Objects.isNull(address)) {
            throw new IllegalArgumentException("address cannot be null!");
        }

        String host = address.getHostString();
        if (Objects.equals(host, "localhost")) {
            host = "127.0.0.1";
        }

        return host + StrUtil.COLON + address.getPort();
    }

    public static InetSocketAddress ToInetSocketAddress(String address) {
        if (StrUtil.isBlank(address)) {
            throw new IllegalArgumentException("address cannot be null!");
        }

        String[] split = address.split(StrUtil.COLON);
        if (split.length != 2) {
            throw new IllegalArgumentException("invalid address");
        }

        return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
    }
}
