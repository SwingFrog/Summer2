package com.swingfrog.summer2.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author: toke
 */
public interface Repository<K, V> {

    void add(V value);
    void add(Collection<V> values);
    void remove(V value);
    void remove(Collection<V> values);
    void removeAll();
    void update(V value);
    void update(Collection<V> values);
    V get(K key);
    List<V> list(Map<String, Object> indexOptional, Predicate<V> filter);
    List<V> listAll();

}
