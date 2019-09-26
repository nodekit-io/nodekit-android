package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

import java.util.Iterator;

/**
 * A JavaScript iterator interface shadow object
 *
 * @param <T> Parameterized type of iterator elements
 *
 */
public class JSIterator<T> extends JSObjectWrapper implements Iterator<T> {
    /**
     * Represents the object returned by 'next'
     *
     *
     */
    public class Next extends JSObjectWrapper {
        protected Next(JSObject next) {
            super(next);
        }

        /**
         * Tests if there are any more elements in the array
         *
         * @return true if more elements to iterate, false otherwise
         */
        public boolean done() {
            return getJSObject().property("done").jsvalueToBoolean();
        }

        /**
         * Returns the JSValue of the iterated element
         *
         * @return the value returned from next()
         */
        public JSValue value() {
            return getJSObject().property("value");
        }
    }

    /**
     * Wraps a JavaScript iterator in a Java iterator
     *
     * @param iterator the JavaScript iterator object.  Assumes the object is a properly formed JS
     *                 iterator
     */
    public JSIterator(JSObject iterator) {
        super(iterator);
        next = _jsnext();
    }

    private Next next;

    private Next _jsnext() {
        return new Next(getJSObject().property("next").jsvalueToFunction().call(getJSObject()).jsvalueToObject());
    }

    /**
     * The 'next' JavaScript iterator object
     *
     * @return the next JSObject in the JSIterator
     */
    public Next jsnext() {
        Next ret = next;
        next = _jsnext();
        return ret;
    }

    /**
     * @return next value in the iterator
     * @see Iterator#next()
     */
    @Override
    @SuppressWarnings("unchecked")
    public T next() {
        return (T) jsnext().value();
    }

    /**
     * @return true if next() will return a value, false if no values left
     * @see Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return !next.done();
    }

    /**
     * @throws UnsupportedOperationException always
     * @see Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
