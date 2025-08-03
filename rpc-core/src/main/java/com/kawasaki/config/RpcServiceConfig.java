package com.kawasaki.config;

// used by server to register services: by defining service name + version + group only one specific service is spotted

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcServiceConfig {
    private Object service;
    private String version = "";
    private String group = "";

    public RpcServiceConfig(Object service) {
        this.service = service;
    }

    public List<String> serviceNames() {
        return interfaceNames().stream()
                .map(interfaceName -> interfaceName + getVersion() + getGroup())
                .collect(Collectors.toList());
    }

    // get the names of all interfaces implemented by the service
    private List<String> interfaceNames() {
        return Arrays.stream(service.getClass().getInterfaces())
                .map(Class::getCanonicalName)
                .collect(Collectors.toList());
    }
}
