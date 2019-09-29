package io.nodekit.engine.javascriptcore;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class JSErrorTest {

    @Test
    public void TestJSErrorAndJSException() throws Exception {
        JSContext context = new JSContext();

        JSError error = new JSError(context, "This is an error message");
        assertThat(error.message(),is("This is an error message"));
        assertThat(error.name(),is("Error"));
        assertThat(error.stack(),is("undefined"));

        JSError error2 = new JSError(context, "Message2");
        assertThat(error2.message(),is("Message2"));
        assertThat(error2.name(),is("Error"));
        assertThat(error2.stack(),is("undefined"));

        JSError error3 = new JSError(context);
        assertThat(error3.message(),is(""));
        assertThat(error3.name(),is("Error"));
        assertThat(error3.stack(),is("undefined"));

        JSFunction fail = new JSFunction(context, "_fail", new String[] {},
                "var undef; var foo = undef.accessme;",
                "fail.js", 10) {
        };
        try {
            fail.call();
            assertFalse(true); // should not get here
        } catch (JSException e) {
            JSError error4 = e.getError();
            assertThat(error4.message(),is("undefined is not an object (evaluating 'undef.accessme')"));
            assertThat(e.getMessage(),is("undefined is not an object (evaluating 'undef.accessme')"));
            assertThat(e.toString(),is("TypeError: undefined is not an object (evaluating 'undef.accessme')"));
            assertThat(error4.name(),is("TypeError"));
            assertThat(e.name(),is("TypeError"));
            assertThat(error4.stack(),is("_fail@fail.js:11:27"));
            assertThat(e.stack(),is("_fail@fail.js:11:27"));
        }

        try {
            context.property("_error_",error);
            context.evaluateScript("throw _error_;",null,"main.js",1);
            assertFalse(true); // should not get here
        } catch (JSException e) {
            JSError error4 = e.getError();
            assertThat(error4.message(),is("This is an error message"));
            assertThat(e.getMessage(),is("This is an error message"));
            assertThat(e.toString(),is("Error: This is an error message"));
            assertThat(error4.name(),is("Error"));
            assertThat(e.name(),is("Error"));
            assertThat(error4.stack(),is("undefined"));
            assertThat(e.stack(),is("undefined"));
        }

        try {
            throw new JSException(error);
        } catch (JSException e) {
            JSError error5 = e.getError();
            assertThat(error5.message(),is("This is an error message"));
            assertThat(e.getMessage(),is("This is an error message"));
            assertThat(e.toString(),is("Error: This is an error message"));
            assertThat(error5.name(),is("Error"));
            assertThat(e.name(),is("Error"));
            assertThat(error5.stack(),is("undefined"));
            assertThat(e.stack(),is("undefined"));
        }

        try {
            throw new JSException(context,"Another exception");
        } catch (JSException e) {
            JSError error5 = e.getError();
            assertThat(error5.message(),is("Another exception"));
            assertThat(e.getMessage(),is("Another exception"));
            assertThat(e.toString(),is("Error: Another exception"));
            assertThat(error5.name(),is("Error"));
            assertThat(e.name(),is("Error"));
            assertThat(error5.stack(),is("undefined"));
            assertThat(e.stack(),is("undefined"));
        }

        try {
            throw new JSException(context,null);
        } catch (JSException e) {
            assertNotNull(e.getError());
            assertNull(e.getMessage());
            assertThat(e.toString(),is("Error: null"));
            assertThat(e.name(),is("Error"));
            assertThat(e.stack(),is("undefined"));
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedFunctions() {
        boolean exception = false;
        try {
            new JSError(new JSContext(), "foo", "bar.js", 1);
        } catch(UnsupportedOperationException e) {
            exception = true;
        }
        assertTrue(exception);
    }

    @org.junit.After
    public void shutDown() {
        Runtime.getRuntime().gc();
    }
}