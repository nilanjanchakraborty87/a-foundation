package com.ajjpj.afoundation.collection;

import java.util.Iterator;


/**
 * This is an iterator that 'concatenates' other iterators. When it reaches the end of one of the contained iterators,
 *  it continues with the first element of the next iterator.
 *
 * @author arno
 */
public class ACompositeIterator<T> implements Iterator<T> {
    private final Iterator<Iterator<T>> itit;

    public ACompositeIterator(Iterator<Iterator<T>> itit) {
        this.itit = itit;
    }

    public ACompositeIterator(Iterable<Iterator<T>> itit) {
        this.itit = itit.iterator();
    }

    @SuppressWarnings("unchecked")
    private Iterator<T> curIterator = (Iterator<T>) EMPTY;

    @Override
    public boolean hasNext() {
        refreshCurrent ();
        return curIterator.hasNext();
    }

    @Override
    public T next() {
        refreshCurrent ();
        return curIterator.next();
    }

    private void refreshCurrent () {
        while(!curIterator.hasNext() && itit.hasNext()) {
            curIterator = itit.next();
        }
    }

    @Override
    public void remove() {
        curIterator.remove();
    }

    private static final Iterator<Object> EMPTY = new Iterator<Object>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
}
