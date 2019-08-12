package es.amplia.oda.core.commons.utils;

import java.util.*;

public class MapBasedDictionary<K, V> extends Dictionary<K, V> {

    private final Map<K, V> innerMap;
    private final Class<K> keyClass;


    private MapBasedDictionary(Map<K, V> innerMap, Class<K> keyClass) {
        this.innerMap = innerMap;
        this.keyClass = keyClass;
    }

    public MapBasedDictionary(Class<K> keyClass) {
        this(new HashMap<>(), keyClass);
    }

    public static <K, V> MapBasedDictionary<K, V> of(Dictionary dictionary, Class<K> keyClass, Class<V> valueClass) {
        Map<K, V> map = new HashMap<>(dictionary.size());
        Enumeration keys = dictionary.keys();
        Enumeration values = dictionary.elements();
        while (keys.hasMoreElements() && values.hasMoreElements()) {
            map.put(keyClass.cast(keys.nextElement()), valueClass.cast(values.nextElement()));
        }
        return new MapBasedDictionary<>(map, keyClass);
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    static class IteratorEnumeration<E> implements Enumeration<E> {

        private final Iterator<E> innerIterator;


        IteratorEnumeration(Iterator<E> innerIterator) {
            this.innerIterator = innerIterator;
        }

        @Override
        public boolean hasMoreElements() {
            return innerIterator.hasNext();
        }

        @Override
        public E nextElement() {
            return innerIterator.next();
        }
    }

    @Override
    public Enumeration<K> keys() {
        return new IteratorEnumeration<>(innerMap.keySet().iterator());
    }

    @Override
    public Enumeration<V> elements() {
        return new IteratorEnumeration<>(innerMap.values().iterator());
    }

    @Override
    public V get(Object key) {
        return innerMap.get(keyClass.cast(key));
    }

    @Override
    public V put(K key, V value) {
        return innerMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return innerMap.remove(keyClass.cast(key));
    }
}
