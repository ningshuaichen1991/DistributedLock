package com.distributedLock.enums;

/**
 * 分布式锁参数类的下标
 * @Classname LockParameterIndex
 * @Description TODO
 * INDEX_0 代表第一个参数类
 */
public enum  LockParameterIndex {

    INDEX_0(0),
    INDEX_1(1),
    INDEX_2(2),
    INDEX_3(3),
    INDEX_4(4),
    INDEX_5(5),
    INDEX_6(6),
    INDEX_7(7),
    INDEX_8(8),
    INDEX_9(9);

    private int index;

    private LockParameterIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
