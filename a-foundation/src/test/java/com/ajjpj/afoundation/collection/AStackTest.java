package com.ajjpj.afoundation.collection;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.mutable.AStack;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AStackTest {
    @Test
    public void testEmpty() {
        final AStack<String> stack = new AStack<>();

        assertEquals(0, stack.size());

        assertTrue(stack.isEmpty());
        assertFalse(stack.nonEmpty());

        assertFalse(stack.tryPeek().isDefined());
        assertFalse(stack.tryPop().isDefined());

        assertFalse(stack.contains("a"));
    }

    @Test
    public void testPushPop() {
        final AStack<String> stack = new AStack<>();

        stack.push("a");
        assertEquals(1, stack.size());
        assertFalse(stack.isEmpty());
        assertTrue(stack.nonEmpty());

        assertEquals("a", stack.tryPeek().get());
        assertEquals("a", stack.peek());
        assertEquals(1, stack.size());

        assertEquals("a", stack.pop());
        assertEquals(0, stack.size());

        stack.push("x");
        stack.push("y");
        stack.push("z");

        assertEquals(3, stack.size());

        final Iterator<String> iter = stack.iterator();
        assertTrue(iter.hasNext());
        assertEquals("z", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("y", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("x", iter.next());

        assertFalse(iter.hasNext());

        assertEquals(3, stack.size());

        assertEquals(AOption.some("z"), stack.tryPeek());
        assertEquals(3, stack.size());
        assertEquals(AOption.some("z"), stack.tryPop());
        assertEquals(2, stack.size());
        assertEquals(AOption.some("y"), stack.tryPeek());
        assertEquals(2, stack.size());
        assertEquals(AOption.some("y"), stack.tryPop());
        assertEquals(1, stack.size());
        assertEquals(AOption.some("x"), stack.tryPeek());
        assertEquals(1, stack.size());
        assertEquals(AOption.some("x"), stack.tryPop());
        assertEquals(0, stack.size());
        assertEquals(AOption.<String>none(), stack.tryPeek());
        assertEquals(AOption.<String>none(), stack.tryPop());
        assertEquals(AOption.<String>none(), stack.tryPeek());
    }
}
