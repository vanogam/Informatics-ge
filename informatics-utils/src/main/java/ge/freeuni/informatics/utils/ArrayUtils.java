package ge.freeuni.informatics.utils;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    public static <E> List<E> getPage(List<E> list, Integer offset, Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = list.size();
        }
        if (offset >= list.size()) {
            return new ArrayList<>();
        }
        if (offset < 0) {
            offset = 0;
        }
        return list.subList(offset,
                Math.min(list.size(), offset + limit));
    }
}
