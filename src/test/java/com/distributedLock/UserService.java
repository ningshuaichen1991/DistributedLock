package com.distributedLock;

public class UserService {

    public synchronized void addUser(String userName){
        System.out.println("添加用户");
    }
}