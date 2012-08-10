package polyglot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {
    public static <T> List<T> copy(List<? extends T> src, boolean immutable) {
        if (src == null) return null;
        List<T> result = new ArrayList<T>(src);
        if (immutable) return Collections.unmodifiableList(result);
        return result;
    }
}
