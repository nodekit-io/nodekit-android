package io.nodekit.engine.javascriptcore;

import android.os.Handler;
import android.os.Looper;

import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class JSContextTest {

    public interface JSContextInterface {
        @SuppressWarnings("unused")
        int func1();
    }
    public class JSContextClass extends JSContext implements JSContextInterface {
        public JSContextClass() {
            super(JSContextInterface.class);
        }
        @Override
        public int func1() {
            return 55;
        }
    }
    public class JSContextInGroup extends JSContext implements JSContextInterface {
        public JSContextInGroup(JSContextGroup inGroup) {
            super(inGroup, JSContextInterface.class);
        }
        @Override
        public int func1() {
            return property("testObject").jsvalueToFunction().call().jsvalueToNumber().intValue();
        }
    }

    @org.junit.Test
    public void testJSContextConstructor() throws Exception {
        JSContext context = new JSContext();
        context.property("test",10);
        assertTrue(context.property("test").jsvalueToNumber().equals(10.0));

        JSContext context1 = new JSContextClass();
        JSValue ret = context1.evaluateScript("func1()");
        assertTrue(ret.jsvalueToNumber().equals(55.0));

        JSContextGroup contextGroup = new JSContextGroup();
        JSContext context2 = new JSContext(contextGroup);
        JSContext context3 = new JSContext(contextGroup);
        context2.evaluateScript("var forty_two = 42; var cx2_func = function() { return forty_two; };");
        JSValue cx2_func = context2.property("cx2_func");
        context3.property("cx3_func", cx2_func);
        JSValue forty_two = context3.evaluateScript("cx3_func()");
        assertTrue(forty_two.jsvalueToNumber().equals(42.0));

        JSContextInGroup context4 = new JSContextInGroup(contextGroup);
        context4.property("testObject", cx2_func);
        ret = context4.evaluateScript("func1()");
        assertTrue(ret.jsvalueToNumber().equals(42.0));

        assertEquals(context2.getGroup(),context3.getGroup());
        assertEquals(context3.getGroup(),context4.getGroup());
        assertNotEquals(context4.getGroup(),null);
        assertNotEquals(context1.getGroup(),context2.getGroup());

        context.garbageCollect();
        context1.garbageCollect();
        context2.garbageCollect();
        context3.garbageCollect();
        context4.garbageCollect();
    }

    private boolean excp;

    @org.junit.Test
    public void testJSContextExceptionHandler() throws Exception {
        JSContext context = new JSContext();
        try {
            context.property("does_not_exist").jsvalueToFunction();
            assertTrue(false);
        } catch (JSException e) {
            assertTrue(true);
        }

        excp = false;
        context.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException e) {
                excp = !excp;
            }
        });
        try {
            context.property("does_not_exist").jsvalueToFunction();
            assertTrue(excp);
        } catch (JSException e) {
            assertTrue(false);
        }

        context.clearExceptionHandler();
        try {
            context.property("does_not_exist").jsvalueToFunction();
            assertTrue(false);
        } catch (JSException e) {
            // excp should still be true
            assertTrue(excp);
        }

        excp = false;
        final JSContext context2 = new JSContext();
        context2.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException e) {
                excp = !excp;
                // Raise another exception.  Should throw JSException
                context2.property("does_not_exist").jsvalueToFunction();
            }
        });
        try {
            context2.property("does_not_exist").jsvalueToFunction();
            assertTrue(false);
        } catch (JSException e) {
            assertTrue(excp);
        }

        context.garbageCollect();
        context2.garbageCollect();
    }

    @org.junit.Test
    public void testJSContextEvaluateScript() throws Exception {
        final String script1 = "" +
                "var val1 = 1;\n" +
                "var val2 = 'foo';\n" +
                "does_not_exist(do_something);";
        String url = "http://liquidplayer.com/script1.js";

        JSContext context = new JSContext();
        try {
            context.evaluateScript(script1, null, url, 1);
            assertTrue(false);
        } catch (JSException e) {
            String stack = e.getError().jsvalueToObject().property("stack").toString();
            String expected = "global code@" + url + ":3:";
            assertEquals(stack.substring(0,expected.length()),expected);
        }

        context.property("localv",69);
        JSValue val = context.evaluateScript("this.localv",null);
        assertTrue(val.jsvalueToNumber().equals(69.0));
        JSObject obj = new JSObject(context);
        obj.property("localv",100);
        val = context.evaluateScript("this.localv",obj);
        assertTrue(val.jsvalueToNumber().equals(100.0));

        val = context.evaluateScript("this.localv");
        assertTrue(val.jsvalueToNumber().equals(69.0));

        context.garbageCollect();
    }

    Exception thrownInMainThread = null;

    @org.junit.Test
    public void testMainThreadSupport() throws Exception {
        Handler handler = new Handler(Looper.getMainLooper());
        final Semaphore mutex = new Semaphore(0);
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSContextTest test = new JSContextTest();
                    test.testJSContextConstructor();
                    test.testJSContextEvaluateScript();
                    test.testJSContextExceptionHandler();
                } catch (Exception e) {
                    thrownInMainThread = e;
                } finally {
                    mutex.release();
                }
            }
        });
        mutex.acquireUninterruptibly();
        if (thrownInMainThread != null) throw thrownInMainThread;
    }

    @Test
    public void testDeadReferences() throws Exception {
        JSContext context = new JSContext();
        for (int i=0; i<200; i++) {
            new JSValue(context);
        }
        assertTrue(true);
    }

    @org.junit.After
    public void shutDown() {
        Runtime.getRuntime().gc();
    }
}