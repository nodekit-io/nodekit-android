package io.nodekit.engine.javascriptcore;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JSUint32ArrayTest {
    private JSContext context;

    @Before
    public void setUp() throws Exception {
        context = new JSContext();
    }

    @Test
    public void testJSUint32Array() throws Exception {
        JSUint32Array array = new JSUint32Array(context,8);
        assertThat(array.byteLength(),is(8*4));
        assertThat(array.byteOffset(),is(0));
        assertTrue(JSTypedArray.isTypedArray(array));
        assertEquals(array.property("BYTES_PER_ELEMENT").jsvalueToNumber().intValue(),4);

        JSUint32Array i8 = new JSUint32Array(context,8);
        for (int i=0; i<8; i++) i8.set(i,(long)i);
        JSUint32Array array2 = new JSUint32Array(i8);
        for (int i=0; i<8; i++)
            assertEquals(array2.get(i),Long.valueOf((long)i));

        List<Long> ai = Arrays.asList(0L,1L,2L,3L,4L,5L,6L,7L);
        JSUint32Array array3 = new JSUint32Array(context,ai);
        context.property("array2",array2);
        context.property("array3",array3);
        assertEquals(array2.jsvalueIsEqual(array3),context.evaluateScript("array2==array3").jsvalueToBoolean());
        assertEquals(array2,array3);
        assertThat(array3.size(),is(8));
        assertThat(array3.get(0),is((long)0));
        assertThat(array3.get(1),is((long)1));

        JSUint32Array ab = new JSUint32Array(context,8);
        for (int i=0; i<8; i++) ab.set(i,(long)i);

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        JSUint32Array array4 = new JSUint32Array(ab.buffer(),16,2);
        assertThat(array4.size(),is(2));
        assertThat(array4.get(0),is((long)4));
        assertThat(array4.get(1),is((long)5));

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        JSUint32Array array5 = new JSUint32Array(ab.buffer(),24);
        assertThat(array5.size(),is(2));
        assertThat(array5.get(0),is((long)6));
        assertThat(array5.get(1),is((long)7));

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        JSUint32Array array6 = new JSUint32Array(ab.buffer());
        assertThat(array6.size(),is(8));
        assertThat(array6.get(0),is((long)0));
        assertThat(array6.get(1),is((long)1));
        assertThat(ab.buffer().byteLength(),is(array6.byteLength()));

        JSUint32Array array7 = new JSUint32Array(context,Arrays.asList(5,4,3,2,1));
        JSUint32Array array8 = array7.subarray(0,2);
        assertThat(array8.size(),is(2));
        assertThat(array8.get(0),is((long)5));
        assertThat(array8.get(1),is((long)4));

        JSUint32Array array9 = array7.subarray(2);
        assertThat(array9.size(),is(3));
        assertThat(array9.get(0),is((long)3));
        assertThat(array9.get(1),is((long)2));
        assertThat(array9.get(2),is((long)1));

        context.evaluateScript("var Uint32a = new Uint32Array(10);");
        assertEquals(context.property("Uint32a").jsvalueToObject().getClass(),JSUint32Array.class);

        List<Long> list = new JSUint32Array(context, Arrays.asList(1,2,3,4,5));

        boolean exception = false;
        try {
            list.add((long)6);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        Object[] toarray = list.toArray();
        assertEquals(toarray[0],(long)1);
        assertEquals(toarray[1],(long)2);
        assertEquals(toarray[2],(long)3);

        assertThat(list.get(0),is((long)1));
        assertThat(list.get(1),is((long)2));
        assertThat(list.get(2),is((long)3));

        assertThat(list.size(),is(5));

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Long> list2 = new JSUint32Array(context,0);
        assertFalse(list.isEmpty());
        assertTrue(list2.isEmpty());

        assertTrue(list.contains((long)1));
        assertTrue(list.contains((long)2));
        assertTrue(list.contains((long)2));
        assertFalse(list.contains((long)6));

        int i = 0;
        for (Iterator<Long> it = list.iterator(); it.hasNext(); i++) {
            Long next = it.next();
            assertTrue(list.contains(next));
        }
        assertThat(i,is(list.size()));

        Long[] arr1 = new Long[5];
        Long[] arr2 = list.toArray(arr1);
        assertArrayEquals(arr2,new Long[] {(long)1,(long)2,(long)3,(long)4,(long)5});

        exception = false;
        try {
            list.remove(2);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.remove((long)2);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        Collection<Long> collection = new ArrayList<>();
        collection.add((long)2);
        collection.add((long)3);
        collection.add((long)4);
        Collection<Long> collection2 = new ArrayList<>(collection);
        collection2.add((long)25);
        assertTrue(list.containsAll(collection));
        assertFalse(list.containsAll(collection2));

        exception = false;
        try {
            list.addAll(collection);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.removeAll(collection);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.retainAll(collection);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.clear();
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.clear();
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        Long last1;
        try {
            list.set(10, (long)10);
            last1 = (long)0;
        } catch (IndexOutOfBoundsException e) {
            last1 = list.set(1, (long)20);
        }
        assertTrue(last1.equals((long)2));
        assertTrue(list.get(1).equals((long)20));

        exception = false;
        try {
            list.add(1, (long)30);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        exception = false;
        try {
            list.remove(4);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        list = new JSUint32Array(context,Arrays.asList(0,1,2,3,0,1,2,3));
        assertThat(list.indexOf((long)0),is(0));
        assertThat(list.indexOf((long)1),is(1));
        assertThat(list.indexOf((long)2),is(2));

        assertThat(list.lastIndexOf((long)0),is(4));
        assertThat(list.lastIndexOf((long)1),is(5));
        assertThat(list.lastIndexOf((long)2),is(6));

        ListIterator<Long> it = list.listIterator();
        it.next();
        it.set((long)100);
        assertTrue(list.get(0).equals((long)100));

        exception = false;
        try {
            it.add((long)20);
        } catch (UnsupportedOperationException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }

        ListIterator<Long> it2 = list.listIterator(1);
        assertTrue(it2.next().equals((long)1));

        assertEquals(list.subList(1, 4),Arrays.asList((long)1,(long)2,(long)3));

        exception = false;
        try {
            list.subList(-1,0);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }
        exception = false;
        try {
            list.subList(100,101);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }
        exception = false;
        try {
            list.subList(3,2);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        } finally {
            assertThat(exception,is(true));
        }
    }
}