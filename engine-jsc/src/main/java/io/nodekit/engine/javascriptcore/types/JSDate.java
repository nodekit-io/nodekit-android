package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;
import io.nodekit.engine.javascriptcore.util.JSException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Convenience class for managing JavaScript date objects
 *
 *
 */
public class JSDate extends JSObject {
    /**
     * Creates a new date object with the current date and time
     *
     * @param ctx The JSContext in which to create the date object
     *
     */
    public JSDate(JSContext ctx) {
        context = ctx;
        valueRef = testException(this.NJSmakeDate(context.contextRef(), new long[0]));
        context.persistObject(this);
    }

    /**
     * Creates a new date object, initialized with a Java timestamp
     *
     * @param ctx  The JSContext in which to create the date object
     * @param date The Date with which to initialize the object
     *
     */
    public JSDate(JSContext ctx, Date date) {
        context = ctx;
        JSValue time = new JSValue(context, date.getTime());
        long[] args = {time.valueRef()};
        valueRef = testException(this.NJSmakeDate(context.contextRef(), args));
        context.persistObject(this);
    }

    /**
     * Creates a new date object, initialized with a Java timestamp
     *
     * @param ctx   The JSContext in which to create the date object
     * @param epoch Milliseconds since since 1 January 1970 00:00:00 UTC
     *
     */
    public JSDate(JSContext ctx, Long epoch) {
        context = ctx;
        JSValue time = new JSValue(context, epoch);
        long[] args = {time.valueRef()};
        valueRef = testException(this.NJSmakeDate(context.contextRef(), args));
        context.persistObject(this);
    }

    /**
     * Creates a new data object, initialized by date components
     *
     * @param ctx    The JSContext in which to create the date object
     * @param params FullYear, Month[, Date[, Hours[, Minutes[, Seconds[, Milliseconds]]]]]
     *
     */
    public JSDate(JSContext ctx, Integer... params) {
        context = ctx;
        long[] p = new long[Math.max(params.length, 7)];
        for (int i = 0; i < 7; i++) {
            if (i < params.length) p[i] = new JSValue(context, params[i]).valueRef();
        }
        valueRef = testException(this.NJSmakeDate(context.contextRef(), p));
        context.persistObject(this);
    }

    private long testException(JNIReturnObject jni) {
        if (jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            return (NJSmake(context.contextRef(), 0L));
        } else {
            return jni.reference;
        }
    }

    /* Methods */

    /**
     * JavaScript Date.now(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/now
     *
     * @param ctx The JavaScript context
     * @return Returns the numeric value corresponding to the current time - the number of milliseconds
     * elapsed since 1 January 1970 00:00:00 UTC.
     *
     */
    public static Long now(JSContext ctx) {
        return ctx.property("Date").jsvalueToObject().property("now").jsvalueToFunction().call().jsvalueToNumber().longValue();
    }

    /**
     * JavaScript Date.parse(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/parse
     * Parses a string representation of a date and returns the number of milliseconds since 1
     * January, 1970, 00:00:00, UTC.
     * Note: Parsing of strings with Date.parse is strongly discouraged due to browser differences
     * and inconsistencies.
     *
     * @param ctx    The context into which to create the JavaScript date object
     * @param string String representation of the date
     * @return a new JavaScript date object
     *
     */
    public static Long parse(JSContext ctx, String string) {
        return ctx.property("Date").jsvalueToObject().property("parse").jsvalueToFunction().call(null, string)
                .jsvalueToNumber().longValue();
    }

    /**
     * JavaScript Date.UTC(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/UTC
     * Accepts the same parameters as the longest form of the constructor (i.e. 2 to 7) and returns
     * the number of milliseconds since 1 January, 1970, 00:00:00 UTC.
     *
     * @param ctx    The context into which to create the JavaScript date object
     * @param params FullYear, Month[, Date[, Hours[, Minutes[, Seconds[, Milliseconds]]]]]
     * @return a new JavaScript date object
     *
     */
    public static Long UTC(JSContext ctx, Integer... params) {
        ArrayList<Integer> p = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (i < params.length) p.add(params[i]);
            else p.add(0);
        }
        return ctx.property("Date").jsvalueToObject().property("UTC").jsvalueToFunction().apply(null, p.toArray())
                .jsvalueToNumber().longValue();
    }

    /* Date.prototype Methods */

    /* Getter */

    /**
     * JavaScript Date.prototype.getDate(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getDate
     *
     * @return Returns the day of the month (1-31) for the specified date according to local time.
     *
     */
    public Integer getDate() {
        return property("getDate").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getDay(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getDay
     *
     * @return Returns the day of the week (0-6) for the specified date according to local time.
     *
     */
    public Integer getDay() {
        return property("getDay").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getFullYear(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getFullYear
     *
     * @return Returns the year (4 digits for 4-digit years) of the specified date according to local time.
     *
     */
    public Integer getFullYear() {
        return property("getFullYear").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getHours(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getHours
     *
     * @return Returns the hour (0-23) in the specified date according to local time.
     *
     */
    public Integer getHours() {
        return property("getHours").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getMilliseconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getMilliseconds
     *
     * @return Returns the milliseconds (0-999) in the specified date according to local time.
     *
     */
    public Integer getMilliseconds() {
        return property("getMilliseconds").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getMinutes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getMinutes
     *
     * @return Returns the minutes (0-59) in the specified date according to local time.
     *
     */
    public Integer getMinutes() {
        return property("getMinutes").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getMonth(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getMonth
     *
     * @return Returns the month (0-11) in the specified date according to local time.
     *
     */
    public Integer getMonth() {
        return property("getMonth").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getSeconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getSeconds
     *
     * @return Returns the seconds (0-59) in the specified date according to local time.
     *
     */
    public Integer getSeconds() {
        return property("getSeconds").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getTime(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTime
     *
     * @return Returns the numeric value of the specified date as the number of milliseconds
     * since January 1, 1970, 00:00:00 UTC (negative for prior times).
     *
     */
    public Long getTime() {
        return property("getTime").jsvalueToFunction().call(this).jsvalueToNumber().longValue();
    }

    /**
     * JavaScript Date.prototype.getTimezoneOffset(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTimezoneOffset
     *
     * @return Returns the time-zone offset in minutes for the current locale.
     *
     */
    public Integer getTimezoneOffset() {
        return property("getTimezoneOffset").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCDate(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCDate
     *
     * @return Returns the day (date) of the month (1-31) in the specified date according to
     * universal time.
     *
     */
    public Integer getUTCDate() {
        return property("getUTCDate").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCDay(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCDay
     *
     * @return Returns the day of the week (0-6) in the specified date according to universal time.
     *
     */
    public Integer getUTCDay() {
        return property("getUTCDay").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCFullYear(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCFullYear
     *
     * @return Returns the year (4 digits for 4-digit years) in the specified date according to
     * universal time.
     *
     */
    public Integer getUTCFullYear() {
        return property("getUTCFullYear").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCHours(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCHours
     *
     * @return Returns the hours (0-23) in the specified date according to universal time.
     *
     */
    public Integer getUTCHours() {
        return property("getUTCHours").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCMilliseconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCMilliseconds
     *
     * @return Returns the milliseconds (0-999) in the specified date according to universal time.
     *
     */
    public Integer getUTCMilliseconds() {
        return property("getUTCMilliseconds").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCMinutes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCMinutes
     *
     * @return Returns the minutes (0-59) in the specified date according to universal time.
     *
     */
    public Integer getUTCMinutes() {
        return property("getUTCMinutes").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCMonth(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCMonth
     *
     * @return Returns the month (0-11) in the specified date according to universal time.
     *
     */
    public Integer getUTCMonth() {
        return property("getUTCMonth").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /**
     * JavaScript Date.prototype.getUTCSeconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCSeconds
     *
     * @return Returns the seconds (0-59) in the specified date according to universal time.
     *
     */
    public Integer getUTCSeconds() {
        return property("getUTCSeconds").jsvalueToFunction().call(this).jsvalueToNumber().intValue();
    }

    /* Setter */

    /**
     * JavaScript Date.prototype.setDate(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setDate
     *
     * @param value Sets the day of the month for a specified date according to local time.
     *
     */
    public void setDate(Integer value) {
        property("setDate").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setFullYear(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setFullYear
     *
     * @param value Sets the full year (e.g. 4 digits for 4-digit years) for a specified date
     *              according to local time.
     *
     */
    public void setFullYear(Integer value) {
        property("setFullYear").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setHours(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setHours
     *
     * @param value Sets the hours for a specified date according to local time.
     *
     */
    public void setHours(Integer value) {
        property("setHours").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setMilliseconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setMilliseconds
     *
     * @param value Sets the milliseconds for a specified date according to local time.
     *
     */
    public void setMilliseconds(Integer value) {
        property("setMilliseconds").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setMinutes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setMinutes
     *
     * @param value Sets the minutes for a specified date according to local time.
     *
     */
    public void setMinutes(Integer value) {
        property("setMinutes").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setMonth(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setMonth
     *
     * @param value Sets the month for a specified date according to local time.
     *
     */
    public void setMonth(Integer value) {
        property("setMonth").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setSeconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setSeconds
     *
     * @param value Sets the seconds for a specified date according to local time.
     *
     */
    public void setSeconds(Integer value) {
        property("setSeconds").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setTime(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setTime
     *
     * @param value Sets the Date object to the time represented by a number of milliseconds since
     *              January 1, 1970, 00:00:00 UTC, allowing for negative numbers for times prior.
     *
     */
    public void setTime(Long value) {
        property("setTime").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCDate(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCDate
     *
     * @param value Sets the day of the month for a specified date according to universal time.
     *
     */
    public void setUTCDate(Integer value) {
        property("setUTCDate").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCFullYear(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCFullYear
     *
     * @param value Sets the full year (e.g. 4 digits for 4-digit years) for a specified date
     *              according to universal time.
     *
     */
    public void setUTCFullYear(Integer value) {
        property("setUTCFullYear").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCHours(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCHours
     *
     * @param value Sets the hour for a specified date according to universal time.
     *
     */
    public void setUTCHours(Integer value) {
        property("setUTCHours").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCMilliseconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCMilliseconds
     *
     * @param value Sets the milliseconds for a specified date according to universal time.
     *
     */
    public void setUTCMilliseconds(Integer value) {
        property("setUTCMilliseconds").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCMinutes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCMinutes
     *
     * @param value Sets the minutes for a specified date according to universal time.
     *
     */
    public void setUTCMinutes(Integer value) {
        property("setUTCMinutes").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCMonth(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCMonth
     *
     * @param value Sets the month for a specified date according to universal time.
     *
     */
    public void setUTCMonth(Integer value) {
        property("setUTCMonth").jsvalueToFunction().call(this, value);
    }

    /**
     * JavaScript Date.prototype.setUTCSeconds(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setUTCSeconds
     *
     * @param value Sets the seconds for a specified date according to universal time.
     *
     */
    public void setUTCSeconds(Integer value) {
        property("setUTCSeconds").jsvalueToFunction().call(this, value);
    }

    /* Conversion getter */

    /**
     * JavaScript Date.prototype.toDateString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toDateString
     *
     * @return Returns the "date" portion of the Date as a human - readable string.
     *
     */
    public String toDateString() {
        return property("toDateString").jsvalueToFunction().call(this).toString();
    }

    /**
     * JavaScript Date.prototype.toISOString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString
     *
     * @return Converts a date to a string following the ISO 8601 Extended Format.
     *
     */
    public String toISOString() {
        return property("toISOString").jsvalueToFunction().call(this).toString();
    }

    /**
     * JavaScript Date.prototype.jsvalueToJSON(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toJSON
     *
     * @return Returns a string representing the Date using toISOString(). Intended for use
     * by JSON.stringify().
     *
     */
    public String jsvalueToJSON() {
        return property("toJSON").jsvalueToFunction().call(this).toString();
    }

    /**
     * JavaScript Date.prototype.toTimeString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toTimeString
     *
     * @return Returns the "time" portion of the Date as a human - readable string.
     *
     */
    public String toTimeString() {
        return property("toTimeString").jsvalueToFunction().call(this).toString();
    }

    /**
     * JavaScript Date.prototype.toUTCString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toUTCString
     *
     * @return Converts a date to a string using the UTC timezone.
     *
     */
    public String toUTCString() {
        return property("toUTCString").jsvalueToFunction().call(this).toString();
    }
}
