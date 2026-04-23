package top.niunaijun.blackbox.utils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 数组工具：提供裁剪、查找、包含判断、类型索引、空判断等常见操作。
 */
public class ArrayUtils {

    /** 将数组裁剪为指定长度，size 为 0 返回 null。 */
    public static<T> T[] trimToSize(T[] array, int size) {
        if (array == null || size == 0) {
            return null;
        } else if (array.length == size) {
            return array;
        } else {
            return Arrays.copyOf(array, size);
        }
    }

    /** 追加单个元素到对象数组尾部。 */
    public static Object[] push(Object[] array, Object item)
    {
        Object[] longer = new Object[array.length + 1];
        System.arraycopy(array, 0, longer, 0, array.length);
        longer[array.length] = item;
        return longer;
    }

    /** 是否包含某元素（对象相等）。 */
    public static <T> boolean contains(T[] array, T value) {
        return indexOf(array, value) != -1;
    }
    /** 是否包含某 int 值。 */
    public static boolean contains(int[] array, int value) {
        if (array == null) return false;
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    /** 返回对象在数组中的索引，未找到为 -1。 */
    public static <T> int indexOf(T[] array, T value) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], value)) return i;
        }
        return -1;
    }

    /** 在 Class<?> 数组中查找精确类型位置。 */
    public static int protoIndexOf(Class<?>[] array, Class<?> type) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == type) return i;
        }
        return -1;
    }

    /** 查找对象数组中首个精确类型匹配的元素索引。 */
    public static int indexOfFirst(Object[] array, Class<?> type) {
        if (!isEmpty(array)) {
            int N = -1;
            for (Object one : array) {
                N++;
                if (one != null && type == one.getClass()) {
                    return N;
                }
            }
        }
        return -1;
    }

    /** 在 Class<?> 数组中从给定序数位置开始查找精确类型。 */
    public static int protoIndexOf(Class<?>[] array, Class<?> type, int sequence) {
        if (array == null) {
            return -1;
        }
        while (sequence < array.length) {
            if (type == array[sequence]) {
                return sequence;
            }
            sequence++;
        }
        return -1;
    }

    /** 在对象数组中，从 sequence 开始查找第一个 isInstance 命中的位置。 */
    public static int indexOfObject(Object[] array, Class<?> type, int sequence) {
        if (array == null) {
            return -1;
        }
        while (sequence < array.length) {
            if (type.isInstance(array[sequence])) {
                return sequence;
            }
            sequence++;
        }
        return -1;
    }

    /** 在对象数组中查找第 sequence 次出现的精确类型位置。 */
    public static int indexOf(Object[] array, Class<?> type, int sequence) {
        if (!isEmpty(array)) {
            int N = -1;
            for (Object one : array) {
                N++;
                if (one != null && one.getClass() == type) {
                    if (--sequence <= 0) {
                        return N;
                    }
                }
            }
        }
        return -1;
    }

    /** 返回对象数组中最后一次出现精确类型的位置。 */
    public static int indexOfLast(Object[] array, Class<?> type) {
        if (!isEmpty(array)) {
            for (int N = array.length; N > 0; N--) {
                Object one = array[N - 1];
                if (one != null && one.getClass() == type) {
                    return N - 1;
                }
            }
        }
        return -1;
    }

    /** 数组是否为空或长度为 0。 */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /** 返回数组中首个指定类型元素的值。 */
    @SuppressWarnings("unchecked")
    public static <T> T getFirst(Object[] args, Class<?> clazz) {
        int index = indexOfFirst(args, clazz);
        if (index != -1) {
            return (T) args[index];
        }
        return null;
    }

    /** 检查偏移与长度是否在数组范围之内。 */
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) throws ArrayIndexOutOfBoundsException {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
    }
}
