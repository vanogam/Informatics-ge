package ge.freeuni.informatics.utils;

import java.util.List;

public class ArrayUtils {

    public static <E> List<E> getPage(List<E> list, Integer offset, Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = list.size();
        }
        return list.subList(Math.min(offset, list.size() - 1),
                Math.min(list.size() - 1, offset + limit));
    }
}
