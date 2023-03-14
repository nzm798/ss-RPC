package com.ss.extension;

/**
 * 实现多线程之间可以修改一个值，并被改变
 *
 * @param <T>
 */
public class Holder<T> {
    private volatile T value; //内存可见性是指当一个线程修改了某个变量的值，其它线程总是能知道这个变量变化。

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
