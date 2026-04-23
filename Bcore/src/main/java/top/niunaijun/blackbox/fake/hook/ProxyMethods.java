package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * 标注多个代理方法名，便于同一个 MethodHook 复用到多方法。
 */
public @interface ProxyMethods {
    String[] value() default {};
}
