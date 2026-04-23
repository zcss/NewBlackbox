package top.niunaijun.blackbox.utils;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 反射工具：提供链式 API 定位并操作构造器、字段、方法。
 * 支持静默模式 QuietReflector，在反射失败时记录异常而不抛出，便于兼容不同系统版本。
 */
public class Reflector {
    public static final String LOG_TAG = "Reflector";

    protected Class<?> mType;
    protected Object mCaller;
    protected Constructor mConstructor;
    protected Field mField;
    protected Method mMethod;

    /** 通过类名创建反射器。 */
    public static Reflector on(String name) throws Exception {
        return on(name, true, Reflector.class.getClassLoader());
    }

    /** 通过类名创建反射器，可控制是否初始化类。 */
    public static Reflector on(String name, boolean initialize) throws Exception {
        return on(name, initialize, Reflector.class.getClassLoader());
    }

    /** 通过类名与类加载器创建反射器。 */
    public static Reflector on(String name, boolean initialize, ClassLoader loader) throws Exception {
        try {
            return on(Class.forName(name, initialize, loader));
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    /** 通过类型创建反射器。 */
    public static Reflector on(Class<?> type) {
        Reflector reflector = new Reflector();
        reflector.mType = type;
        return reflector;
    }

    /** 基于实例创建反射器并绑定调用者。 */
    public static Reflector with(Object caller) throws Exception {
        return on(caller.getClass()).bind(caller);
    }

    protected Reflector() {

    }

    /** 定位构造器并准备 newInstance。 */
    public Reflector constructor(Class<?>... parameterTypes) throws Exception {
        try {
            mConstructor = mType.getDeclaredConstructor(parameterTypes);
            mConstructor.setAccessible(true);
            mField = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    /** 调用已定位构造器创建实例。 */
    @SuppressWarnings("unchecked")
    public <R> R newInstance(Object... initargs) throws Exception {
        if (mConstructor == null) {
            throw new Exception("Constructor was null!");
        }
        try {
            return (R) mConstructor.newInstance(initargs);
        } catch (InvocationTargetException e) {
            throw new Exception("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    protected Object checked(Object caller) throws Exception {
        if (caller == null || mType.isInstance(caller)) {
            return caller;
        }
        throw new Exception("Caller [" + caller + "] is not a instance of type [" + mType + "]!");
    }

    protected void check(Object caller, Member member, String name) throws Exception {
        if (member == null) {
            throw new Exception(name + " was null!");
        }
        if (caller == null && !Modifier.isStatic(member.getModifiers())) {
            throw new Exception("Need a caller!");
        }
        checked(caller);
    }

    /** 绑定调用者（用于非静态字段/方法）。 */
    public Reflector bind(Object caller) throws Exception {
        mCaller = checked(caller);
        return this;
    }

    /** 取消绑定调用者。 */
    public Reflector unbind() {
        mCaller = null;
        return this;
    }

    /** 定位字段。 */
    public Reflector field(String name) throws Exception {
        try {
            mField = findField(name);
            mField.setAccessible(true);
            mConstructor = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    protected Field findField(String name) throws NoSuchFieldException {
        try {
            return mType.getField(name);
        } catch (NoSuchFieldException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredField(name);
                } catch (NoSuchFieldException ex) {

                }
            }
            throw e;
        }
    }

    /** 读取已定位字段（使用已绑定调用者）。 */
    @SuppressWarnings("unchecked")
    public <R> R get() throws Exception {
        return get(mCaller);
    }

    /** 读取字段值。 */
    @SuppressWarnings("unchecked")
    public <R> R get(Object caller) throws Exception {
        check(caller, mField, "Field");
        try {
            return (R) mField.get(caller);
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    /** 设置已定位字段（使用已绑定调用者）。 */
    public Reflector set(Object value) throws Exception {
        return set(mCaller, value);
    }

    /** 设置字段值。 */
    public Reflector set(Object caller, Object value) throws Exception {
        check(caller, mField, "Field");
        try {
            mField.set(caller, value);
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    /** 定位方法。 */
    public Reflector method(String name, Class<?>... parameterTypes) throws Exception {
        try {
            mMethod = findMethod(name, parameterTypes);
            mMethod.setAccessible(true);
            mConstructor = null;
            mField = null;
            return this;
        } catch (NoSuchMethodException e) {
            throw new Exception("Oops!", e);
        }
    }

    protected Method findMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return mType.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException ex) {

                }
            }
            throw e;
        }
    }

    /** 调用已定位方法（使用已绑定调用者）。 */
    public <R> R call(Object... args) throws Exception {
        return callByCaller(mCaller, args);
    }

    /** 调用方法。 */
    @SuppressWarnings("unchecked")
    public <R> R callByCaller(Object caller, Object... args) throws Exception {
        check(caller, mMethod, "Method");
        try {
            return (R) mMethod.invoke(caller, args);
        } catch (InvocationTargetException e) {
            throw new Exception("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }

    /**
     * 静默反射器：捕获并保存异常到 mIgnored，而非抛出，方便在不同系统/ROM 上做兼容性尝试。
     */
    public static class QuietReflector extends Reflector {

        protected Throwable mIgnored;

        /** 通过类名创建静默反射器。 */
        public static QuietReflector on(String name) {
            return on(name, true, QuietReflector.class.getClassLoader());
        }

        /** 通过类名创建静默反射器，可控制是否初始化类。 */
        public static QuietReflector on(String name, boolean initialize) {
            return on(name, initialize, QuietReflector.class.getClassLoader());
        }

        /** 通过类名与类加载器创建静默反射器。 */
        public static QuietReflector on(String name, boolean initialize, ClassLoader loader) {
            Class<?> cls = null;
            try {
                cls = Class.forName(name, initialize, loader);
                return on(cls, null);
            } catch (Throwable e) {

                return on(cls, e);
            }
        }

        /** 通过类型创建静默反射器。 */
        public static QuietReflector on(Class<?> type) {
            return on(type, (type == null) ? new Exception("Type was null!") : null);
        }

        private static QuietReflector on(Class<?> type, Throwable ignored) {
            QuietReflector reflector = new QuietReflector();
            reflector.mType = type;
            reflector.mIgnored = ignored;
            return reflector;
        }

        /** 基于实例创建静默反射器并绑定调用者。 */
        public static QuietReflector with(Object caller) {
            if (caller == null) {
                return on((Class<?>) null);
            }
            return on(caller.getClass()).bind(caller);
        }

        protected QuietReflector() {

        }

        /** 返回最近一次操作忽略的异常。 */
        public Throwable getIgnored() {
            return mIgnored;
        }

        protected boolean skip() {
            return skipAlways() || mIgnored != null;
        }

        protected boolean skipAlways() {
            return mType == null;
        }

        @Override
        public QuietReflector constructor(Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.constructor(parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public <R> R newInstance(Object... initargs) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.newInstance(initargs);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return null;
        }

        @Override
        public QuietReflector bind(Object obj) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.bind(obj);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public QuietReflector unbind() {
            super.unbind();
            return this;
        }

        @Override
        public QuietReflector field(String name) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.field(name);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public <R> R get() {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get();
            } catch (Throwable e) {
                mIgnored = e;

            }
            return null;
        }

        @Override
        public <R> R get(Object caller) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get(caller);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return null;
        }

        @Override
        public QuietReflector set(Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(value);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public QuietReflector set(Object caller, Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(caller, value);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public QuietReflector method(String name, Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.method(name, parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return this;
        }

        @Override
        public <R> R call(Object... args) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.call(args);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return null;
        }

        @Override
        public <R> R callByCaller(Object caller, Object... args) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.callByCaller(caller, args);
            } catch (Throwable e) {
                mIgnored = e;

            }
            return null;
        }
    }

    /**
     * 按方法名返回首个匹配的方法（当存在重载时不区分参数，取声明顺序中的第一个）。
     */
    public static Method findMethodByFirstName(Class<?> clazz, String methodName) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (methodName.equals(declaredMethod.getName())) {
                return declaredMethod;
            }
        }
        return null;
    }
}
