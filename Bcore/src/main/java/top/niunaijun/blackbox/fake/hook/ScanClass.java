package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * 指定额外扫描的类，其内部声明类上的 ProxyMethod(s) 会被一并注册。
 */
public @interface ScanClass {
    Class<?>[] value() default {};
}