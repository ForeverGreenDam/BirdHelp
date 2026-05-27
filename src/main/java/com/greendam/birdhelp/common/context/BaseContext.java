package com.greendam.birdhelp.common.context;

/**
 * BaseContext is a utility class that provides a thread-local storage for the current user ID.
 * @author ForeverGreenDam
 */
public class BaseContext {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> nameThreadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static String getCurrentName() {
        return nameThreadLocal.get();
    }

    public static void setCurrentName(String name) {
        nameThreadLocal.set(name);
    }

    public static void removeCurrentId() {
        threadLocal.remove();
        nameThreadLocal.remove();
    }

}
