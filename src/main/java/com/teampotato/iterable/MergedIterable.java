package com.teampotato.iterable;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class MergedIterable<T> implements Iterable<T> {
    private final Iterable<T> iterable1;
    private final Iterable<T> iterable2;

    public MergedIterable(Iterable<T> iterable1, Iterable<T> iterable2) {
        this.iterable1 = iterable1;
        this.iterable2 = iterable2;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new MergedIterator<>(iterable1.iterator(), iterable2.iterator());
    }
}

class MergedIterator<T> implements Iterator<T> {
    private final Iterator<T> iterator1;
    private final Iterator<T> iterator2;
    private boolean useIterator1;

    public MergedIterator(Iterator<T> iterator1, Iterator<T> iterator2) {
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
        this.useIterator1 = true;
    }

    @Override
    public boolean hasNext() {
        return (useIterator1 && iterator1.hasNext()) || iterator2.hasNext();
    }

    @Override
    public T next() {
        if (useIterator1) {
            if (iterator1.hasNext()) {
                return iterator1.next();
            } else {
                useIterator1 = false;
            }
        }
        return iterator2.next();
    }
}