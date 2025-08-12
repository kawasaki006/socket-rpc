package com.kawasaki.server.service;

import cn.hutool.core.util.IdUtil;
import com.kawasaki.annotation.Limit;
import com.kawasaki.api.User;
import com.kawasaki.api.UserService;

public class UserServiceImpl implements UserService {
    @Limit(permitsPerSecond = 5, timeout = 0)
    @Override
    public User getUser(Long id) {
        return User.builder()
                .id(id)
                .name(IdUtil.fastSimpleUUID())
                .build();
    }
}
