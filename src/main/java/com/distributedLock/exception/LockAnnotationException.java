package com.distributedLock.exception;

/**
 * 注解异常
 * @Classname LockParameterException
 * @Description TODO
 */
public class LockAnnotationException extends LockException{

    public LockAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockAnnotationException(String message) {
        super(message);
    }

    public LockAnnotationException(Throwable cause) {
        super(cause);
    }
}
