package com.mobvoi.knowledgegraph.wearcontroltetris;

/**
 * Created by lili on 15-4-20.
 */

import java.util.Iterator;

public class CircularDoubleArray {
    private double[] array;
    private int len;

    public boolean isFull() {
        return len == array.length;
    }

    private int start;

    public CircularDoubleArray(int capacity) {
        array = new double[capacity];
    }

    public int size() {
        return len;
    }

    public int getCapacity() {
        return array.length;
    }

    public Iterator<Double> iterator() {
        return new Itr();
    }

    public void clear() {
        start = len = 0;
    }

    public double[] toArray() {
        double[] result = new double[len];
        Iterator<Double> iter = this.iterator();
        int idx = 0;
        while (iter.hasNext()) {
            result[idx++] = iter.next();
        }
        return result;
    }

    public class Itr implements Iterator<Double> {
        private int curr;
        private int c = 0;

        public Itr() {
            if (len == array.length) {
                curr = start;
            } else {
                curr = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return c < len;
        }

        @Override
        public Double next() {
            Double f = array[curr];
            curr = (curr + 1) % array.length;
            c++;
            return f;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }

    public void add(double f) {
        if (len != array.length) {
            len++;
        }
        array[start] = f;
        start = (start + 1) % array.length;
    }
}