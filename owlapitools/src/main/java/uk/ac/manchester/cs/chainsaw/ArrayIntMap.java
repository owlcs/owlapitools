package uk.ac.manchester.cs.chainsaw;

import java.util.ArrayList;
import java.util.List;

public class ArrayIntMap {
    private final List<FastSet> map = new ArrayList<FastSet>();

    /** @param key
     * @param value */
    public void put(int key, int value) {
        if (key >= map.size()) {
            while (key >= map.size()) {
                map.add(null);
            }
        }
        FastSet set = map.get(key);
        if (set == null) {
            set = new FastSetSimple();
            map.set(key, set);
        }
        set.add(value);
    }

    /** returns a mutable set of values connected to the key; if no value is
     * connected, returns an immutable empty set
     * 
     * @param key
     * @return the set of values connected with the key */
    public FastSet get(int key) {
        if (key < map.size()) {
            FastSet collection = map.get(key);
            if (collection != null) {
                return collection;
            }
        }
        return new FastSetSimple();
    }

    /** @return the set of keys */
    public List<Integer> keySet() {
        List<Integer> toReturn = new ArrayList<Integer>();
        for (int i = 0; i < map.size(); i++) {
            if (map.get(i) != null) {
                toReturn.add(i);
            }
        }
        return toReturn;
    }

    // /**
    // * @return all values in the map
    // */
    public List<Integer> getAllValues() {
        List<Integer> list = new ArrayList<Integer>();
        for (FastSet f : map) {
            if (f != null) {
                for (int i = 0; i < f.size(); i++) {
                    list.add(f.get(i));
                }
            }
        }
        return list;
    }

    //
    // /**
    // * removes the set of values connected to the key
    // *
    // * @param key
    // */
    // public boolean remove(Key key) {
    // if (this.map.remove(key) != null) {
    // size = -1;
    // return true;
    // }
    // return false;
    // }
    // /**
    // * removes the value connected to the key; if there is more than one value
    // * connected to the key, only one is removed
    // *
    // * @param key
    // * @param value
    // */
    // public boolean remove(Key key, Value value) {
    // Collection<Value> c = this.map.get(key);
    // if (c != null) {
    // boolean toReturn = c.remove(value);
    // // if false, no change was actually made - skip the rest
    // if (!toReturn) {
    // return false;
    // }
    // size = -1;
    // if (c.isEmpty()) {
    // this.map.remove(key);
    // }
    // return true;
    // }
    // return false;
    // }
    /** @return the size of the multimap (sum of all the sizes of the sets) */
    // public int size() {
    // if (size < 0) {
    // size = getAllValues().size();
    // }
    // return this.size;
    // }
    // /**
    // * @param k
    // * @param v
    // * @return true if the pairing (k, v) is in the map (set equality for v)
    // */
    // public boolean contains(Key k, Value v) {
    // final Collection<Value> collection = this.map.get(k);
    // if (collection == null) {
    // return false;
    // }
    // return collection.contains(v);
    // }
    /** @param k
     * @return true if k is a key for the map */
    public boolean containsKey(int k) {
        return k < map.size() && map.get(k) != null;
    }

    // /**
    // * @param v
    // * @return true if v is a value for a key in the map
    // */
    // public boolean containsValue(Value v) {
    // for (Collection<Value> c : map.values()) {
    // if (c.contains(v)) {
    // return true;
    // }
    // }
    // return false;
    // }
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return "MultiMap " + map.toString();// .replace(",", "\n");
    }
    // public void putAll(MultiMap<Key, Value> otherMap) {
    // for (Key k : otherMap.keySet()) {
    // putAll(k, otherMap.get(k));
    // }
    // }
    //
    // public void putAll(Key k, Collection<Value> v) {
    // Collection<Value> set = map.get(k);
    // if (set == null) {
    // set = createCollection();
    // setEntry(k, set);
    // }
    // set.addAll(v);
    // size=-1;
    // }
    // public boolean isValueSetsEqual() {
    // if (map.size() < 2) {
    // return true;
    // }
    // List<Collection<Value>> list = new
    // ArrayList<Collection<Value>>(map.values());
    // for (int i = 1; i < list.size(); i++) {
    // if (!list.get(0).equals(list.get(i))) {
    // return false;
    // }
    // }
    // return true;
    // }
}
