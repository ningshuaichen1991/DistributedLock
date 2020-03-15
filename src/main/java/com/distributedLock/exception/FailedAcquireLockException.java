package com.distributedLock.exception;

/**
 * 获取锁异常
 * @Classname GetLockException
 */
public class FailedAcquireLockException extends LockException{

    public FailedAcquireLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedAcquireLockException(String message) {
        super(message);
    }

    public FailedAcquireLockException(Throwable cause) {
        super(cause);
    }
}
