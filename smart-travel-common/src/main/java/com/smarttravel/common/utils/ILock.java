package com.smarttravel.common.utils;

public interface ILock {
    boolean tryLock(long timeoutSec);
    void unlock();
}
