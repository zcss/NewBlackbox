package top.niunaijun.blackboxa.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;

public final class ViewBindingEx {
    private ViewBindingEx() {}

    @SuppressWarnings("unchecked")
    public static <T extends ViewBinding> T inflateBinding(LayoutInflater layoutInflater, Class<T> cls) {
        try {
            Method method = cls.getMethod("inflate", LayoutInflater.class);
            return (T) method.invoke(null, layoutInflater);
        } catch (Throwable e) {
            throw new RuntimeException("inflateBinding failed for " + cls, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ViewBinding> T newBindingViewHolder(ViewGroup parent, boolean attachToParent, Class<T> cls) {
        try {
            Method method = cls.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            return (T) method.invoke(null, LayoutInflater.from(parent.getContext()), parent, attachToParent);
        } catch (Throwable e) {
            throw new RuntimeException("newBindingViewHolder failed for " + cls, e);
        }
    }
}
