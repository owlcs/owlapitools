package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** bag for counting occurrences of instances of a specified type
 * 
 * @author ignazio
 * @param <Type>
 *            type */
public class Bag<Type> {
    Map<Type, AtomicLong> map = new LinkedHashMap<Type, AtomicLong>();
    int size = 0;

    /** @return size of the bag (all occurrences) */
    public int size() {
        return size;
    }

    /** @param t
     *            instance to add to the bag
     * @return new number of occurrences */
    public long add(Type t) {
        size++;
        AtomicLong l = map.get(t);
        if (l != null) {
            return l.incrementAndGet();
        } else {
            map.put(t, new AtomicLong(1));
            return 1;
        }
    }

    /** @param t
     *            instance to check
     * @return number of occurrences for t */
    public long check(Type t) {
        AtomicLong l = map.get(t);
        if (l != null) {
            return l.get();
        }
        return 0;
    }

    /** @param increasing
     *            true for ascending sorting
     * @return list of distinct instances */
    public List<Type> sortedList(final boolean increasing) {
        List<Type> toReturn = new ArrayList<Type>(map.keySet());
        Collections.sort(toReturn, new Comparator<Type>() {
            @Override
            public int compare(Type o1, Type o2) {
                if (increasing) {
                    return Long.signum(check(o1) - check(o2));
                } else {
                    return Long.signum(check(o2) - check(o1));
                }
            }
        });
        return toReturn;
    }

    /** @return list of distinct instances */
    public Collection<Type> list() {
        return map.keySet();
    }

    /** @return all occurrences as an array */
    public long[] values() {
        long[] toReturn = new long[map.values().size()];
        int i = 0;
        for (AtomicLong l : map.values()) {
            toReturn[i++] = l.get();
        }
        Arrays.sort(toReturn);
        return toReturn;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
