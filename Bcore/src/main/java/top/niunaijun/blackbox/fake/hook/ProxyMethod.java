package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * 标注单个代理方法名，用于将 MethodHook 绑定到指定方法。
 */
public @interface ProxyMethod {
    String value();
}
