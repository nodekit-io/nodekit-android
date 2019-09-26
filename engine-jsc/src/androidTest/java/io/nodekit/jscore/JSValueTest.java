package io.nodekit.engine.javascriptcore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class JSValueTest {

    @org.junit.Test
    public void testJSValueConstructors() throws Exception {
        JSContext context = new JSContext();

        /**
         * new JSValue(context)
         */
        JSValue undefined = new JSValue(context);
        assertTrue(undefined.jsvalueIsUndefined());

        /**
         * new JSValue(context,null)
         */
        JSValue NULL = new JSValue(context,null);
        assertTrue(NULL.jsvalueIsNull());

        /**
         * new JSValue(context,boolean)
         */
        JSValue bool = new JSValue(context,false);
        assertTrue(bool.jsvalueIsBoolean());

        /**
         * new JSValue(context,number)
         */
        JSValue integer = new JSValue(context,50);
        JSValue doub = new JSValue(context,50.0);
        JSValue lng = new JSValue(context,50L);
        JSValue flt = new JSValue(context,50.f);
        assertTrue(integer.jsvalueIsNumber());
        assertTrue(doub.jsvalueIsNumber());
        assertTrue(lng.jsvalueIsNumber());
        assertTrue(flt.jsvalueIsNumber());

        /**
         * new JSValue(context,string)
         */
        JSValue str1 = new JSValue(context,"This is a string");
        assertTrue(str1.jsvalueIsString());

        /**
         * new JSValue(context,Object)
         */
        JSValue alsoUndefined = new JSValue(context,new Object());
        assertTrue(alsoUndefined.jsvalueIsUndefined());

        /**
         * new JSValue(context,Map)
         */
        Map<String,Integer> map = new HashMap<>();
        map.put("one",1);
        map.put("two",2);
        JSValue mapValue = new JSValue(context,map);
        assertTrue(mapValue.jsvalueIsObject());
        assertEquals(mapValue.jsvalueToObject().property("two").jsvalueToNumber().intValue(),2);

        /**
         * new JSValue(context,List)
         */
        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        JSValue listValue = new JSValue(context,list);
        assertTrue(listValue.isArray());
        assertEquals(listValue.jsvalueToJSArray().get(1),"second");

        /**
         * new JSValue(context,Array)
         */
        String [] array = new String[] {"first", "second", "third"};
        JSValue arrayValue = new JSValue(context,array);
        assertTrue(arrayValue.isArray());
        assertEquals(arrayValue.jsvalueToJSArray().get(2),"third");

        JSValue undefined2 = new JSValue(0L,context);
        assertTrue(undefined2.jsvalueIsUndefined());
    }

    @org.junit.Test
    public void testJSValueTesters() throws Exception {
        final String script =
                "var undefined; \n" +
                        "var NULL = null; \n" +
                        "var bool = true; \n" +
                        "var number = 15.6; \n" +
                        "var string = 'string'; \n" +
                        "var object = {}; \n" +
                        "var array = []; \n" +
                        "var date = new Date(); \n" +
                        "";
        JSContext context = new JSContext();
        context.evaluateScript(script);
        assertFalse(context.property("undefined").jsvalueIsNull());
        assertFalse(context.property("undefined").jsvalueIsBoolean());
        assertFalse(context.property("undefined").jsvalueIsNumber());
        assertFalse(context.property("undefined").jsvalueIsString());
        assertFalse(context.property("undefined").isArray());
        assertFalse(context.property("undefined").jsvalueIsDate());
        assertFalse(context.property("undefined").jsvalueIsObject());
        assertTrue(context.property("undefined").jsvalueIsUndefined());

        assertFalse(context.property("NULL").jsvalueIsUndefined());
        assertFalse(context.property("NULL").jsvalueIsBoolean());
        assertFalse(context.property("NULL").jsvalueIsNumber());
        assertFalse(context.property("NULL").jsvalueIsString());
        assertFalse(context.property("NULL").isArray());
        assertFalse(context.property("NULL").jsvalueIsDate());
        assertFalse(context.property("NULL").jsvalueIsObject());
        assertTrue(context.property("NULL").jsvalueIsNull());

        assertFalse(context.property("bool").jsvalueIsUndefined());
        assertFalse(context.property("bool").jsvalueIsNumber());
        assertFalse(context.property("bool").jsvalueIsString());
        assertFalse(context.property("bool").isArray());
        assertFalse(context.property("bool").jsvalueIsDate());
        assertFalse(context.property("bool").jsvalueIsObject());
        assertFalse(context.property("bool").jsvalueIsNull());
        assertTrue(context.property("bool").jsvalueIsBoolean());

        assertFalse(context.property("number").jsvalueIsUndefined());
        assertFalse(context.property("number").jsvalueIsString());
        assertFalse(context.property("number").isArray());
        assertFalse(context.property("number").jsvalueIsDate());
        assertFalse(context.property("number").jsvalueIsObject());
        assertFalse(context.property("number").jsvalueIsNull());
        assertFalse(context.property("number").jsvalueIsBoolean());
        assertTrue(context.property("number").jsvalueIsNumber());

        assertFalse(context.property("string").jsvalueIsUndefined());
        assertFalse(context.property("string").isArray());
        assertFalse(context.property("string").jsvalueIsDate());
        assertFalse(context.property("string").jsvalueIsObject());
        assertFalse(context.property("string").jsvalueIsNull());
        assertFalse(context.property("string").jsvalueIsBoolean());
        assertFalse(context.property("string").jsvalueIsNumber());
        assertTrue(context.property("string").jsvalueIsString());

        assertFalse(context.property("object").jsvalueIsUndefined());
        assertFalse(context.property("object").isArray());
        assertFalse(context.property("object").jsvalueIsDate());
        assertFalse(context.property("object").jsvalueIsNull());
        assertFalse(context.property("object").jsvalueIsBoolean());
        assertFalse(context.property("object").jsvalueIsNumber());
        assertFalse(context.property("object").jsvalueIsString());
        assertTrue(context.property("object").jsvalueIsObject());

        assertFalse(context.property("array").jsvalueIsUndefined());
        assertFalse(context.property("array").jsvalueIsDate());
        assertFalse(context.property("array").jsvalueIsNull());
        assertFalse(context.property("array").jsvalueIsBoolean());
        assertFalse(context.property("array").jsvalueIsNumber());
        assertFalse(context.property("array").jsvalueIsString());
        assertTrue(context.property("array").jsvalueIsObject());
        assertTrue(context.property("array").isArray());

        assertFalse(context.property("date").jsvalueIsUndefined());
        assertFalse(context.property("date").jsvalueIsNull());
        assertFalse(context.property("date").jsvalueIsBoolean());
        assertFalse(context.property("date").jsvalueIsNumber());
        assertFalse(context.property("date").jsvalueIsString());
        assertFalse(context.property("date").isArray());
        assertTrue(context.property("date").jsvalueIsObject());
        assertTrue(context.property("date").jsvalueIsDate());

        final String script2 =
                "var foo = function() {}; var bar = new foo();";
        context.evaluateScript(script2);
        assertTrue(context.property("bar").jsvalueIsInstanceOfConstructor(context.property("foo").jsvalueToObject()));
        assertFalse(context.property("foo").jsvalueIsInstanceOfConstructor(context.property("bar").jsvalueToObject()));
    }

    @org.junit.Test
    public void testJSValueComparators() throws Exception {
        JSContext context = new JSContext();
        context.property("number",42f);
        assertEquals(context.property("number").jsvalueToNumber().longValue(),42L);
        assertNotEquals(context.property("number").jsvalueToNumber().intValue(),43);

        context.evaluateScript("string = 'string12345';");
        assertEquals(context.property("string").toString(),"string12345");
        assertNotEquals(context.property("string"),context.property("number"));

        context.evaluateScript("var another_number = 42");
        assertEquals(context.property("number"),context.property("another_number"));

        assertFalse(new JSValue(context,0).jsvalueToBoolean());
        assertFalse(new JSValue(context,0).jsvalueIsStrictEqual(false));
        assertEquals(new JSValue(context,1).toString(),"1");
        assertFalse(new JSValue(context,1).jsvalueIsStrictEqual("1"));
        assertEquals(new JSValue(context,1),new JSValue(context,1.0));
        assertTrue(new JSValue(context,1).jsvalueIsStrictEqual(1.0));
        assertFalse(context.evaluateScript("(function () { var foo; return foo === null; })()").jsvalueToBoolean());
        assertEquals(new JSValue(context),new JSValue(context,null));
        assertFalse(new JSValue(context).jsvalueIsStrictEqual(null));
        assertEquals(new JSValue(context,null),(new JSValue(context)));
        assertFalse(new JSValue(context,null).jsvalueIsStrictEqual(new JSValue(context)));
    }

    @org.junit.Test
    public void testJSValueGetters() throws Exception {
        final String script =
                "var undefined; \n" +
                        "var NULL = null; \n" +
                        "var bool = true; \n" +
                        "var number = 15.6; \n" +
                        "var string = 'string'; \n" +
                        "var object = {}; \n" +
                        "var array = []; \n" +
                        "var date = new Date(1970,10,30); \n" +
                        "var func = function(x) {return x+1;};" +
                        "";
        JSContext context = new JSContext();
        context.evaluateScript(script);
        JSValue undefined = context.property("undefined");
        JSValue NULL = context.property("NULL");
        JSValue bool = context.property("bool");
        JSValue number = context.property("number");
        JSValue string = context.property("string");
        JSValue object = context.property("object");
        JSValue array = context.property("array");
        JSValue date = context.property("date");
        JSValue func = context.property("func");
        assertFalse(undefined.jsvalueToBoolean());
        assertFalse(NULL.jsvalueToBoolean());
        assertTrue(bool.jsvalueToBoolean());
        assertTrue(number.jsvalueToBoolean());
        assertTrue(string.jsvalueToBoolean());
        assertTrue(object.jsvalueToBoolean());
        assertTrue(array.jsvalueToBoolean());
        assertTrue(date.jsvalueToBoolean());
        assertTrue(func.jsvalueToBoolean());

        assertEquals(NULL.jsvalueToNumber().intValue(),0);
        assertEquals(bool.jsvalueToNumber().intValue(),1);
        assertTrue(number.jsvalueToNumber().equals(15.6));
        assertTrue(context.evaluateScript("'11.5'").jsvalueToNumber().equals(11.5));
        assertTrue(undefined.jsvalueToNumber().isNaN());
        assertTrue(string.jsvalueToNumber().isNaN());
        assertTrue(object.jsvalueToNumber().isNaN());
        assertTrue(func.jsvalueToNumber().isNaN());
        assertTrue(array.jsvalueToNumber().equals(0.0));
        assertTrue(context.evaluateScript("[1,2,3]").jsvalueToNumber().isNaN());
        assertEquals(date.jsvalueToNumber(),context.evaluateScript("date.getTime()").jsvalueToNumber());

        assertEquals(undefined.toString(),"undefined");
        assertEquals(NULL.toString(),"null");
        assertEquals(bool.toString(),"true");
        assertEquals(context.evaluateScript("false").toString(),"false");
        assertEquals(number.toString(),"15.6");
        assertEquals(string.toString(),"string");

        assertEquals(object.toString(),"[object Object]");

        assertEquals(func.toString(),"function (x) {return x+1;}");
        assertEquals(array.toString(),"");
        assertEquals(context.evaluateScript("[1,2,3]").toString(),"1,2,3");
        assertTrue(date.toString().startsWith("Mon Nov 30 1970"));
        final String script2 =
                "var jsUndefined = JSON.stringify(undefined); \n" +
                        "var jsNULL = JSON.stringify(NULL); \n" +
                        "var jsBool = JSON.stringify(bool); \n" +
                        "var jsNumber = JSON.stringify(number); \n" +
                        "var jsString = JSON.stringify(string); \n" +
                        "var jsObject = JSON.stringify(object); \n" +
                        "var jsArray = JSON.stringify(array); \n" +
                        "var jsDate = JSON.stringify(date); \n" +
                        "var jsFunc = JSON.stringify(func); \n" +
                        "";
        context.evaluateScript(script2);
        assertEquals(bool.jsvalueToJSON(),context.property("jsBool").toString());
        assertEquals(number.jsvalueToJSON(),context.property("jsNumber").toString());
        assertEquals(string.jsvalueToJSON(),context.property("jsString").toString());
        assertEquals(object.jsvalueToJSON(),context.property("jsObject").toString());
        assertEquals(func.jsvalueToJSON(),null);
        assertEquals(array.jsvalueToJSON(),context.property("jsArray").toString());
        assertEquals(date.jsvalueToJSON(),context.property("jsDate").toString());
        assertEquals(undefined.jsvalueToJSON(),null);
        assertEquals(NULL.jsvalueToJSON(),context.property("jsNULL").toString());

        /**
         * jsvalueToObject()
         */
        assertNotEquals(object.jsvalueToObject(),null);
        assertNotEquals(func.jsvalueToObject(),null);
        assertNotEquals(array.jsvalueToObject(),null);
        assertNotEquals(date.jsvalueToObject(),null);

        /**
         * jsvalueToFunction()
         */
        assertNotEquals(func.jsvalueToFunction(),null);
        assertEquals(func.jsvalueToFunction().call(null,5).jsvalueToNumber().intValue(),6);

        /**
         * jsvalueToJSArray()
         */
        assertNotEquals(array.jsvalueToJSArray(),null);
        assertThat(array.jsvalueToJSArray().size(),is(0));
    }

    @org.junit.After
    public void shutDown() {
        Runtime.getRuntime().gc();
    }
}