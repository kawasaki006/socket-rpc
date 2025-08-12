package com.kawasaki.api;

import com.kawasaki.annotation.Limit;
import com.kawasaki.annotation.Retry;

public interface UserService {
    @Retry
    User getUser(Long id);
}
