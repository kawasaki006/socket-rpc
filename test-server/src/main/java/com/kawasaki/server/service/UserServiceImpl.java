package com.kawasaki.server.service;

import cn.hutool.core.util.IdUtil;
import com.kawasaki.api.User;
import com.kawasaki.api.UserService;

public class UserServiceImpl implements UserService {
    public User getUser(Long id) {
        return User.builder()
                .id(id)
                .name(IdUtil.fastSimpleUUID())
                .build();
    }
}
