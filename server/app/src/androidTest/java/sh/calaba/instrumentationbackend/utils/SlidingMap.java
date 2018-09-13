package sh.calaba.instrumentationbackend.utils;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/*
    A map that contains a fixed set of entries, removing the oldest ones on addition.
 */
public class SlidingMap<K,V> extends AbstractMap<K,V> {
    private Map<K,V> map = new LinkedHashMap<K,V>();
    private int maxSize;

    public SlidingMap(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public synchronized V put(K key, V value) {
        if (map.size() >= maxSize) {
            map.remove(map.keySet().iterator().next());
        }

        map.remove(key);
        map.put(key, value);

        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}