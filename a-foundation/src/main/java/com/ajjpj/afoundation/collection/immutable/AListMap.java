package com.ajjpj.afoundation.collection.immutable;

import com.ajjpj.afoundation.collection.AEquality;
import com.ajjpj.afoundation.collection.tuples.ATuple2;
import com.ajjpj.afoundation.function.AFunction1;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;


/**
 * This AMap implementation stores entries in a linked list, giving all lookup operations O(n) complexity. That makes
 *  AHashMap the better choice most of the time.<p>
 *
 * This class is however useful when keys are known to have the same hash codes (e.g. deep in the innards of AHashMap),
 *  or if control over iteration order is desirable.
 *
 * @author arno
 */
public class AListMap <K,V> extends AbstractAMap<K,V> implements AMapEntry<K,V> {
    private static final AEquality DEFAULT_EQUALITY = AEquality.EQUALS;

    private static final AListMap<Object, Object> emptyEquals = new AListMap<>(AEquality.EQUALS);
    private static final AListMap<Object, Object> emptyIdentity = new AListMap<>(AEquality.IDENTITY);

    /**
     * Returns an empty AListMap instance with default (i.e. equals-based) equalityForEquals. Calling this factory method instead
     *  of the constructor allows internal reuse of empty map instances since they are immutable.
     */
    public static <K,V> AListMap<K,V> empty() {
        return empty(DEFAULT_EQUALITY);
    }
    /**
     * Returns an empty AListMap instance with a given equalityForEquals. Calling this factory method instead
     *  of the constructor allows internal reuse of empty map instances since they are immutable.
     */
    @SuppressWarnings("unchecked")
    public static <K,V> AListMap<K,V> empty(AEquality equality) {
        if(equality == AEquality.EQUALS) return (AListMap<K, V>) emptyEquals;
        if(equality == AEquality.IDENTITY) return (AListMap<K, V>) emptyIdentity;

        return new AListMap<> (equality);
    }

    /**
     * Returns an AListMap instance with default (i.e. equals-based) equalityForEquals, initializing it from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <K,V> AListMap<K,V> fromKeysAndValues(Iterable<ATuple2<K,V>> elements) {
        return fromKeysAndValues(DEFAULT_EQUALITY, elements);
    }
    /**
     * Returns an AHashMap instance with a given equalityForEquals, initializing it from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <K,V> AListMap<K,V> fromKeysAndValues(AEquality equality, Iterable<ATuple2<K,V>> elements) {
        AListMap<K,V> result = empty(equality);

        for(ATuple2<K,V> el: elements) {
            result = result.updated(el._1, el._2);
        }
        return result;
    }

    public static <K,V> AListMap<K,V> fromKeysAndValues(Iterable<K> keys, Iterable<V> values) {
        return fromKeysAndValues(DEFAULT_EQUALITY, keys, values);
    }

    /**
     * Returns an AListMap instance with a given equalityForEquals, initializing it from separate 'keys'
     *  and 'values' collections. Both collections are iterated exactly once, and are expected to have the same size.
     */
    public static <K,V> AListMap<K,V> fromKeysAndValues(AEquality equality, Iterable<K> keys, Iterable<V> values) {
        final Iterator<K> ki = keys.iterator();
        final Iterator<V> vi = values.iterator();

        AListMap<K,V> result = empty (equality);

        while(ki.hasNext()) {
            final K key = ki.next();
            final V value = vi.next();

            result = result.updated(key, value);
        }
        return result;
    }

    final AEquality equality;

    private Integer cachedHashcode = null; // intentionally not volatile - potentially recalculating for different threads is traded for better single threaded performance

    private AListMap(AEquality equality) {
        this.equality = equality;
    }

    @Override public AEquality keyEquality () {
        return equality;
    }

    @Override public AMap<K, V> clear () {
        return empty (equality);
    }

    @Override public int size() {
        return 0;
    }

    @Override public AOption<V> get(K key) {
        return AOption.none();
    }

    @Override public K getKey() {
        throw new NoSuchElementException("empty map");
    }

    @Override public V getValue() {
        throw new NoSuchElementException("empty map");
    }

    @Override public AListMap<K,V> updated(K key, V value) {
        return new Node<>(key, value, this);
    }

    @Override public AListMap<K,V> removed(K key) {
        return this;
    }

    public AListMap<K,V> tail() {
        return null;
    }

    @Override public Iterator<AMapEntry<K,V>> iterator() {
        return new ListMapIterator<>(this);
    }

    @Override public ASet<K> keys() {
        return new AListSet<> (this);
    }

    static class Node<K,V> extends AListMap<K,V> {
        private final K key;
        private final V value;
        private final AListMap<K,V> tail;

        Node(K key, V value, AListMap<K, V> tail) {
            super(tail.equality);

            this.key = key;
            this.value = value;
            this.tail = tail;
        }

        @Override public K getKey() {
            return key;
        }
        @Override public V getValue() {
            return value;
        }
        @Override public AListMap<K,V> tail() {
            return tail;
        }

        @Override public int size() {
            int result = 0;

            AListMap<K,V> m = this;
            while(m.tail() != null) {
                m = m.tail();
                result += 1;
            }
            return result;
        }

        @Override public AOption<V> get(K key) {
            AListMap<K,V> m = this;

            while(m.nonEmpty()) {
                if(equality.equals(m.getKey (), key)) {
                    return AOption.some(m.getValue ());
                }
                m = m.tail();
            }
            return AOption.none();
        }

        @Override public AListMap<K,V> updated(K key, V value) {
            final AListMap<K,V> m = removed(key);
            return new Node<>(key, value, m);
        }

        @Override public AListMap<K,V> removed(K key) {
            int idx = 0;

            AListMap<K,V> remaining = this;
            while(remaining.nonEmpty()) {
                if(equality.equals(remaining.getKey (), key)) {
                    remaining = remaining.tail();
                    break;
                }
                idx += 1;
                remaining = remaining.tail();
            }

            AListMap<K,V> result = remaining;

            AListMap<K,V> iter = this;
            for (int i=0; i<idx; i++) {
                result = new Node<> (iter.getKey (), iter.getValue (), result);
                iter = iter.tail ();
            }

            if (idx >= size ()) {
                return this;
            }

            return result;
        }
    }

    static class ListMapIterator<K,V> implements Iterator<AMapEntry<K,V>> {
        private AListMap<K,V> pos;

        ListMapIterator(AListMap<K, V> pos) {
            this.pos = pos;
        }

        @Override
        public boolean hasNext() {
            return pos.nonEmpty();
        }

        @Override
        public AMapEntry<K, V> next() {
            final AMapEntry<K,V> result = pos;
            pos = pos.tail();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
