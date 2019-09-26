package io.nodekit.engine.javascriptcore.types;

import io.nodekit.engine.javascriptcore.*;

public abstract class JSObjectWrapper extends JSObject {
    protected JSObjectWrapper(JSObject obj) {
        mJSObject = obj;
        context = obj.getContext();
        valueRef = obj.valueRef();
    }

    private final JSObject mJSObject;

    /**
     * Gets underlying JSObject
     *
     * @return JSObject representing the wrapped object
     *
     */
    public JSObject getJSObject() {
        return mJSObject;
    }
}
