package io.nodekit.engine.javascriptcore;

import static org.junit.Assert.*;

public class JSFunctionTest {

    public interface IFunctionObject2 {
        @SuppressWarnings("unused")
        String [] testFunc(
                JSValue jsvalueParam,
                FunctionObject2 jsobjectParam,
                Integer intParam,
                int intParam2,
                Long longParam,
                long longParam2,
                Float floatParam,
                float floatParam2,
                Double doubleParam,
                double doubleParam2,
                String stringParam,
                Boolean booleanParam,
                Integer[] arrayParam
        );
    }
    public static class FunctionObject2 extends JSObject implements IFunctionObject2 {
        private final JSContext context;
        public FunctionObject2(JSContext ctx) {
            super(ctx, IFunctionObject2.class);
            context = ctx;
            property("hello","is it me you're looking for?");
        }

        public String [] testFunc(
                JSValue jsvalueParam,
                FunctionObject2 jsobjectParam,
                Integer intParam,
                int intParam2,
                Long longParam,
                long longParam2,
                Float floatParam,
                float floatParam2,
                Double doubleParam,
                double doubleParam2,
                String stringParam,
                Boolean booleanParam,
                Integer[] arrayParam
        ) {
            return new String [] {
                    jsvalueParam.toString(),
                    jsobjectParam.jsvalueToJSON(),
                    intParam.toString(),
                    Integer.toString(intParam2),
                    longParam.toString(),
                    Long.toString(longParam2),
                    floatParam.toString(),
                    Float.toString(floatParam2),
                    doubleParam.toString(),
                    Double.toString(doubleParam2),
                    stringParam,
                    booleanParam.toString(),
                    new JSArray<>(context,arrayParam,Object.class).jsvalueToJSON()
            };
        }
        final public String script =
                "(function(jsvalueParam,jsobjectParam,intParam,intParam2,longParam,longParam2, \n" +
                        "    floatParam,floatParam2,doubleParam,doubleParam2,stringParam,booleanParam,arrayParam) \n" +
                        "{ \n" +
                        "    result = [''+jsvalueParam, JSON.stringify(jsobjectParam), ''+intParam, ''+intParam2, \n" +
                        "        ''+longParam, ''+longParam2, ''+floatParam, ''+floatParam2, ''+doubleParam, \n" +
                        "        ''+doubleParam2, stringParam, ''+booleanParam, JSON.stringify(arrayParam)]; \n" +
                        "    return result; \n" +
                        "}) \n";
        public JSFunction nativeFunc() {
            return context.evaluateScript(script).jsvalueToFunction();
        }
        public JSFunction javaFunc() {
            context.property("javaObj",this);
            return context.evaluateScript("javaObj.testFunc").jsvalueToFunction();
        }
    }

    public interface IFunctionObject {
        @SuppressWarnings("unused") void voidFunc();
        @SuppressWarnings("unused") JSValue jsvalueFunc();
        @SuppressWarnings("unused") JSObject jsobjectFunc();
        @SuppressWarnings("unused") Integer intFunc();
        @SuppressWarnings("unused") int intFunc2();
        @SuppressWarnings("unused") Long longFunc();
        @SuppressWarnings("unused") long longFunc2();
        @SuppressWarnings("unused") Float floatFunc();
        @SuppressWarnings("unused") float floatFunc2();
        @SuppressWarnings("unused") Double doubleFunc();
        @SuppressWarnings("unused") double doubleFunc2();
        @SuppressWarnings("unused") String stringFunc();
        @SuppressWarnings("unused") Boolean booleanFunc();
        @SuppressWarnings("unused") Integer[] arrayFunc();
    }
    public static class FunctionObject extends JSObject implements IFunctionObject {
        private final JSContext context;
        public FunctionObject(JSContext ctx) {
            super(ctx, IFunctionObject.class);
            context = ctx;
        }

        @Override
        public void voidFunc() {

        }
        @Override
        public JSValue jsvalueFunc() {
            return new JSValue(context);
        }
        @Override
        public JSObject jsobjectFunc() {
            return new JSObject(context);
        }
        @Override
        public Integer intFunc() {
            return 5;
        }
        @Override
        public Long longFunc() {
            return 6L;
        }
        @Override
        public Float floatFunc() {
            return 7.6f;
        }
        @Override
        public Double doubleFunc() {
            return 8.8;
        }
        @Override
        public String stringFunc() {
            return "string";
        }
        @Override
        public Boolean booleanFunc() {
            return true;
        }
        @Override
        public int intFunc2() {
            return 9;
        }
        @Override
        public long longFunc2() {
            return 10L;
        }
        @Override
        public float floatFunc2() {
            return 17.6f;
        }
        @Override
        public double doubleFunc2() {
            return 18.8;
        }
        @Override
        public Integer[] arrayFunc() {
            return new Integer[] {5,6,7,8};
        }
    }

    public interface IPrototypeObject extends IFunctionObject {
        @SuppressWarnings("unused")
        Integer myValue();
    }
    public static class PrototypeObject extends JSObject implements IPrototypeObject {
        public PrototypeObject(JSContext context) {
            super(context,IPrototypeObject.class);
        }
        @Override
        public Integer myValue() {
            return getThis().property("value").jsvalueToNumber().intValue();
        }
        @Override
        public void voidFunc() {}
        @Override
        public JSValue jsvalueFunc() {
            return new JSValue(context);
        }
        @Override
        public JSObject jsobjectFunc() {
            return new JSObject(context);
        }
        @Override
        public Integer intFunc() {
            return 5;
        }
        @Override
        public Long longFunc() {
            return 6L;
        }
        @Override
        public Float floatFunc() {
            return 7.6f;
        }
        @Override
        public Double doubleFunc() {
            return 8.8;
        }
        @Override
        public String stringFunc() {
            return "string";
        }
        @Override
        public Boolean booleanFunc() {
            return true;
        }
        @Override
        public int intFunc2() {
            return 9;
        }
        @Override
        public long longFunc2() {
            return 10L;
        }
        @Override
        public float floatFunc2() {
            return 17.6f;
        }
        @Override
        public double doubleFunc2() {
            return 18.8;
        }
        @Override
        public Integer[] arrayFunc() {
            return new Integer[] {5,6,7,8};
        }

    }

    public class ConstructorFunction extends JSFunction {
        public ConstructorFunction(JSContext ctx) {
            super(ctx,"constructor");
            prototype(new PrototypeObject(ctx));
        }
        @SuppressWarnings("unused")
        public void constructor(int param) {
            getThis().property("value", param);
        }
    }

    public static class TestInstance extends JSObject {
    }

    public class TestFunction extends JSFunction {
        public TestFunction(JSContext ctx) throws NoSuchMethodException {
            super(ctx,TestFunction.class.getMethod("myFunction",Integer.class),TestInstance.class);
        }
        @SuppressWarnings("unused")
        public void myFunction(Integer x) throws Exception {
            getThis().property("x",x);
        }
    }

    public class TestFunction3 extends JSFunction {
        public TestFunction3(JSContext ctx) throws NoSuchMethodException {
            super(ctx,TestFunction3.class.getMethod("myFunction",Integer.class));
        }
        @SuppressWarnings("unused")
        public void myFunction(Integer x) throws Exception {
            getThis().property("x",x);
        }
    }

    @org.junit.Test
    public void testJSFunctionConstructors() throws Exception {
        JSContext context = new JSContext();

        final String script2 =
                "var empty = {}; \n" +
                        "var constructorObject = function(val) {\n" +
                        "    this.value = val; \n" +
                        "};" +
                        "constructorObject.prototype = { \n" +
                        "   voidFunc:    function() {}, \n" +
                        "   jsvalueFunc: function() { var undef; return undef; }, \n" +
                        "   jsobjectFunc:function() { return {}; }, \n" +
                        "   intFunc:     function() { return 5; }, \n" +
                        "   intFunc2:    function() { return 9; }, \n" +
                        "   longFunc:    function() { return 6; }, \n" +
                        "   longFunc2:   function() { return 10; }, \n" +
                        "   floatFunc:   function() { return 7.6; }, \n" +
                        "   floatFunc2:  function() { return 17.6; }, \n" +
                        "   doubleFunc:  function() { return 8.8; }, \n" +
                        "   doubleFunc2: function() { return 18.8; }, \n" +
                        "   stringFunc:  function() { return 'string'; }, \n" +
                        "   arrayFunc:   function() { return [5,6,7,8]; }, \n" +
                        "   booleanFunc: function() { return true; }, \n" +
                        "   myValue:     function() { return this.value; } \n" +
                        "};";

        ConstructorFunction constructorFunction = new ConstructorFunction(context);
        assertEquals(constructorFunction.prototype().jsvalueToObject().getClass(),PrototypeObject.class);
        context.property("constructorObjectJava", constructorFunction);
        context.evaluateScript(script2);
        JSObject js1   = context.evaluateScript("new constructorObject(5)").jsvalueToObject();
        JSObject java1 = context.evaluateScript("new constructorObjectJava(5)").jsvalueToObject();
        JSObject js2   = context.evaluateScript("new constructorObject(6)").jsvalueToObject();
        JSObject java2 = context.evaluateScript("new constructorObjectJava(6)").jsvalueToObject();
        assertTrue(js1.property("myValue").jsvalueToFunction().call(js1).jsvalueIsStrictEqual(
                java1.property("myValue").jsvalueToFunction().call(java1)
        ));
        assertTrue(js2.property("myValue").jsvalueToFunction().call(js2).jsvalueIsStrictEqual(
                java2.property("myValue").jsvalueToFunction().call(java2)
        ));
        assertTrue(context.evaluateScript("new constructorObject(7).myValue() === new constructorObjectJava(7).myValue()").jsvalueToBoolean());
        assertTrue(context.evaluateScript("new constructorObject(8).myValue() !== new constructorObjectJava(9).myValue()").jsvalueToBoolean());
        assertTrue(context.evaluateScript("new constructorObject(9) instanceof constructorObject").jsvalueToBoolean());
        assertTrue(context.evaluateScript("new constructorObjectJava(10) instanceof constructorObjectJava").jsvalueToBoolean());
        assertFalse(context.evaluateScript("new constructorObject(11) instanceof constructorObjectJava").jsvalueToBoolean());
        assertFalse(context.evaluateScript("new constructorObjectJava(12) instanceof constructorObject").jsvalueToBoolean());
        assertTrue(context.evaluateScript("new constructorObject(9)")
                        .jsvalueIsInstanceOfConstructor(context.property("constructorObject").jsvalueToObject()));
        assertTrue(context.evaluateScript("new constructorObjectJava(10)")
                        .jsvalueIsInstanceOfConstructor(context.property("constructorObjectJava").jsvalueToObject()));
        assertTrue(context.evaluateScript("new constructorObjectJava(11)")
                        .jsvalueIsInstanceOfConstructor(constructorFunction));
        assertFalse(context.evaluateScript("new constructorObject(12)")
                        .jsvalueIsInstanceOfConstructor(constructorFunction));

        context.property("functx", new JSFunction(context,"functionBody") {
            @SuppressWarnings("unused")
            public Integer functionBody(Double value) {
                return value.intValue() + 1;
            }
        });
        assertTrue(context.evaluateScript("functx(13.3)").jsvalueIsStrictEqual(14));

        String url = "http://www.liquidplayer.org/js_func.js";
        JSFunction increment = new JSFunction(context, "increment",
                new String[] {"value"},
                "if (typeof value === 'number') return value + 1;\n" +
                        "else return does_not_exist;\n",
                url, 10);
        assertTrue(increment.call(null,5).jsvalueIsStrictEqual(6));
        try {
            assertTrue(increment.call().jsvalueIsStrictEqual(6));
        } catch (JSException e) {
            String stack = e.getError().jsvalueToObject().property("stack").toString();
            String expected = "increment@" + url + ":12:";
            assertEquals(stack.substring(0,expected.length()),expected);
        }

        TestFunction testFunction = new TestFunction(context);
        JSObject instance = testFunction.newInstance(10);
        assertEquals(instance.getClass(),TestInstance.class);
        assertEquals(instance.property("x").jsvalueToNumber().intValue(),10);

        TestFunction3 testFunction3 = new TestFunction3(context);
        JSObject instance3 = testFunction3.newInstance(10);
        assertEquals(instance3.getClass(),JSObject.class);
        assertEquals(instance3.property("x").jsvalueToNumber().intValue(),10);
    }

    private final static String functionObjectScript =
            "var empty = {}; \n" +
                    "var functionObject = {\n" +
                    "   voidFunc:    function() {}, \n" +
                    "   jsvalueFunc: function() { var undef; return undef; }, \n" +
                    "   jsobjectFunc:function() { return {}; }, \n" +
                    "   intFunc:     function() { return 5; }, \n" +
                    "   intFunc2:    function() { return 9; }, \n" +
                    "   longFunc:    function() { return 6; }, \n" +
                    "   longFunc2:   function() { return 10; }, \n" +
                    "   floatFunc:   function() { return 7.6; }, \n" +
                    "   floatFunc2:  function() { return 17.6; }, \n" +
                    "   doubleFunc:  function() { return 8.8; }, \n" +
                    "   doubleFunc2: function() { return 18.8; }, \n" +
                    "   stringFunc:  function() { return 'string'; }, \n" +
                    "   arrayFunc:   function() { return [5,6,7,8]; }, \n" +
                    "   booleanFunc: function() { return true; } \n" +
                    "};";

    @org.junit.Test
    public void testJSFunctionCallback() throws Exception {
        JSContext context = new JSContext();
        context.evaluateScript(functionObjectScript);
        JSObject functionObject = new FunctionObject(context);
        JSObject functionObjectJS = context.property("functionObject").jsvalueToObject();
        context.property("java", functionObject);
        for (String func : functionObjectJS.propertyNames()) {
            boolean strict = context.evaluateScript("functionObject." + func + "() === java." + func + "()").jsvalueToBoolean();
            assertTrue(functionObjectJS.property(func).jsvalueToFunction().call().jsvalueIsStrictEqual(
                    functionObject.property(func).jsvalueToFunction().call()
                    ) == strict && ((func.equals("jsobjectFunc") && !strict) ||
                            (func.equals("arrayFunc") && !strict) ||
                            strict)
            );
        }
        assertTrue(context.evaluateScript("functionObject.arrayFunc().sort().join('|') === java.arrayFunc().sort().join('|')").jsvalueToBoolean());

        FunctionObject2 functionObject2 = new FunctionObject2(context);
        Object [] params = new Object [] {
                new JSValue(context),
                functionObject2,
                1,
                2,
                3L,
                4L,
                5.5f,
                6.6f,
                7.7,
                8.8,
                "this is a string",
                false,
                new Integer[] {9,10,11,12}
        };
        String string1 = functionObject2.nativeFunc().apply(null,params).jsvalueToJSON();
        String string2 = functionObject2.javaFunc().apply(null,params).jsvalueToJSON();
        assertEquals(string1,string2);
    }

    /* Should raise NoSuchMethodException */
    public /* do not make it static! */ class TestInstance2 extends JSObject {
    }
    /* Should raise IllegalAccessException */
    private class PrivateClassFail extends JSObject {
    }
    /* Should raise InstantiationException */
    public static class InstantiationExceptionFail extends JSObject {
        public InstantiationExceptionFail(Integer foo) throws Exception {
            assertNull(foo);
        }
    }

    public class TestFunction2 extends JSFunction {
        public TestFunction2(JSContext ctx, Class<? extends JSObject> c) throws NoSuchMethodException {
            super(ctx,TestFunction.class.getMethod("myFunction",Integer.class),c);
        }
        @SuppressWarnings("unused")
        public void myFunction(Integer x) throws Exception {
            assertFalse(true);
        }
    }

    @org.junit.Test
    public void testExceptionCases() throws Exception {
        JSContext context = new JSContext();

        boolean exception = false;
        try {
            new TestFunction2(context,TestInstance2.class).newInstance(10);
        } catch (JSException e) {
            exception = true;
        } finally {
            assertTrue(exception);
        }

        exception = false;
        try {
            new TestFunction2(context,PrivateClassFail.class).newInstance(10);
        } catch (JSException e) {
            exception = true;
        } finally {
            assertTrue(exception);
        }

        /* Should raise InvocationTargetException */
        exception = false;
        try {
            JSFunction function = new JSFunction(context,"_InvocationTargetException") {
                @SuppressWarnings("unused")
                public void _InvocationTargetException() throws Exception {
                    throw new Exception();
                }
            };
            function.newInstance(10);
        } catch (JSException e) {
            exception = true;
        } finally {
            assertTrue(exception);
        }

        exception = false;
        try {
            new TestFunction2(context,InstantiationExceptionFail.class).newInstance(10);
        } catch (JSException e) {
            exception = true;
        } finally {
            assertTrue(exception);
        }

        exception = false;
        try {
            new JSFunction(context,"_NoMethod").call();
        } catch (JSException e) {
            exception = true;
        } finally {
            assertTrue(exception);
        }
    }

    @org.junit.After
    public void shutDown() {
        Runtime.getRuntime().gc();
    }
}