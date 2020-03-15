package com.distributedLock.exception;

/**
 * 分布锁总异常
 * @Classname LockException
 * @Description TODO
 */
public class LockException extends Throwable{

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
