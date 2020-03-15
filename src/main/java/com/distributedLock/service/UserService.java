package com.distributedLock.service;

import com.distributedLock.domain.User;

import java.util.Map;

public interface UserService {

    int  insertUser(User user, Map<String,Object> params);
}