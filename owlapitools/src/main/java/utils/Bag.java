package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Bag<Type> {
	Map<Type, AtomicLong> map = new LinkedHashMap<Type, AtomicLong>();

	public Bag() {}

	public long add(Type t) {
		AtomicLong l = map.get(t);
		if (l != null) {
			return l.incrementAndGet();
		} else {
			map.put(t, new AtomicLong(1));
			return 1;
		}
	}

	public long check(Type t) {
		AtomicLong l = map.get(t);
		if (l != null) {
			return l.get();
		}
		return 0;
	}

	public List<Type> sortedList(final boolean increasing) {
		List<Type> toReturn = new ArrayList<Type>(map.keySet());
		Collections.sort(toReturn, new Comparator<Type>() {
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

	public Collection<Type> list() {
		return map.keySet();
	}

	public long[] values(){
		long[] toReturn=new long[map.values().size()];
		int i=0;
		for(AtomicLong l:map.values()) {
			toReturn[i++]=l.get();
		}
		Arrays.sort(toReturn);

		return toReturn;
	}

	@Override
	public String toString() {

		return map.toString();
	}

}
