package top.niunaijun.blackbox.fake.service.base;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.MethodHook;

/**
 * ValueMethodProxy：将目标方法直接替换为固定返回值（mValue）。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class ValueMethodProxy extends MethodHook {

	Object mValue;
	String mName;

	public ValueMethodProxy(String name, Object value) {
		mValue = value;
		mName = name;
	}

	@Override
	protected String getMethodName() {
		return mName;
	}

	@Override
	protected Object hook(Object who, Method method, Object[] args) throws Throwable {
		return mValue;
	}
}
