package decomposition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.util.CollectionFactory;

/** @param <Key>
 * @param <Value>
 * @author ignazio palmisano */
@SuppressWarnings("javadoc")
public class IdentityMultiMap<Key, Value> implements Serializable {
    private static final long serialVersionUID = 30402L;
    private final IdentityHashMap<Key, Collection<Value>> map = new IdentityHashMap<Key, Collection<Value>>();
    private int size = 0;

    /** @param key
     * @param value */
    public boolean put(Key key, Value value) {
        Collection<Value> set = this.map.get(key);
        if (set == null) {
            set = createCollection();
            this.map.put(key, set);
        }
        boolean toReturn = set.add(value);
        if (toReturn) {
            size = -1;
        }
        return toReturn;
    }

    private Collection<Value> createCollection() {
        Collection<Value> toReturn = Collections
                .newSetFromMap(new IdentityHashMap<Value, Boolean>());
        return toReturn;
    }

    /** @param key
     * @param values */
    public void setEntry(Key key, Collection<Value> values) {
        this.map.put(key, values);
        this.size = -1;
    }

    /** returns a mutable set of values connected to the key; if no value is
     * connected, returns an immutable empty set
     * 
     * @param key
     * @return the set of values connected with the key */
    public Collection<Value> get(Key key) {
        final Collection<Value> collection = this.map.get(key);
        if (collection != null) {
            return collection;
        }
        return Collections.emptyList();
    }

    /** @return the set of keys */
    public Set<Key> keySet() {
        return this.map.keySet();
    }

    /** @return all values in the map */
    public Set<Value> getAllValues() {
        Set<Value> toReturn = CollectionFactory.createSet();
        for (Collection<Value> s : this.map.values()) {
            toReturn.addAll(s);
        }
        return toReturn;
    }

    /** removes the set of values connected to the key
     * 
     * @param key */
    public boolean remove(Key key) {
        if (this.map.remove(key) != null) {
            size = -1;
            return true;
        }
        return false;
    }

    /** removes the value connected to the key; if there is more than one value
     * connected to the key, only one is removed
     * 
     * @param key
     * @param value */
    public boolean remove(Key key, Value value) {
        Collection<Value> c = this.map.get(key);
        if (c != null) {
            boolean toReturn = c.remove(value);
            // if false, no change was actually made - skip the rest
            if (!toReturn) {
                return false;
            }
            size = -1;
            if (c.isEmpty()) {
                this.map.remove(key);
            }
            return true;
        }
        return false;
    }

    /** @return the size of the multimap (sum of all the sizes of the sets) */
    public int size() {
        if (size < 0) {
            size = getAllValues().size();
        }
        return this.size;
    }

    /** @param k
     * @param v
     * @return true if the pairing (k, v) is in the map (set equality for v) */
    public boolean contains(Key k, Value v) {
        final Collection<Value> collection = this.map.get(k);
        if (collection == null) {
            return false;
        }
        return collection.contains(v);
    }

    /** @param k
     * @return true if k is a key for the map */
    public boolean containsKey(Key k) {
        return this.map.containsKey(k);
    }

    /** @param v
     * @return true if v is a value for a key in the map */
    public boolean containsValue(Value v) {
        for (Collection<Value> c : map.values()) {
            if (c.contains(v)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        this.map.clear();
        this.size = 0;
    }

    @Override
    public String toString() {
        return "MultiMap " + size() + "\n" + map.toString();
    }

    public void putAll(IdentityMultiMap<Key, Value> otherMap) {
        for (Key k : otherMap.keySet()) {
            putAll(k, otherMap.get(k));
        }
    }

    public void putAll(Key k, Collection<Value> v) {
        Collection<Value> set = map.get(k);
        if (set == null) {
            set = createCollection();
            setEntry(k, set);
        }
        set.addAll(v);
        size = -1;
    }

    public boolean isValueSetsEqual() {
        if (map.size() < 2) {
            return true;
        }
        List<Collection<Value>> list = new ArrayList<Collection<Value>>(map.values());
        for (int i = 1; i < list.size(); i++) {
            if (!list.get(0).equals(list.get(i))) {
                return false;
            }
        }
        return true;
    }
}
