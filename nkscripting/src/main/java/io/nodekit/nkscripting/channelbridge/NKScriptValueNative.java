/*
* nodekit.io
*
* Copyright (c) 2016 OffGrid Networks. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.nodekit.nkscripting.channelbridge;

import io.nodekit.nkscripting.util.NKCallback;
import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.channelbridge.NKScriptTypeInfo.NKScriptTypeInfoMemberInfo;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

class NKScriptValueNative extends NKScriptValue {

    private NKScriptInvocation proxy;

    Object nativeObject;

    private NKScriptChannel _channel;
    private int _instanceid = 0;

    NKScriptValueNative(String ns, NKScriptChannel channel, int instanceid, Object obj) {
        super(ns, channel.context, null);
        this._channel = channel;
        this._instanceid = instanceid;
        this.proxy = bindObject(obj);
    }

    // Create new instance of plugin for given channel
    NKScriptValueNative(String ns, NKScriptChannel channel, int instanceid, Object[] args, boolean create)  {
        super(ns, channel.context, null);
        if (!create)
            throw new IllegalArgumentException();

        this._channel = channel;
        this._instanceid = instanceid;
        Class cls = channel.typeInfo.getType();
        NKScriptTypeInfoMemberInfo constructor = channel.typeInfo.defaultConstructor();

        Object[] argsWrapped = wrapArgs(args);
        NKScriptValue promise = null;

        int arity = constructor.arity;

        Object instance = NKScriptInvocation.construct(cls, constructor.getconstructor(), argsWrapped);

        proxy = bindObject(instance);
    }

   /* protected NKScriptValueNative(String ns, NKScriptChannel channel)  {
        super(ns, channel.context, null);
    }

    public NKScriptValueNative(Object value, NKScriptContext context) {
        super();
        Class t = value.getClass();
        NKScriptChannel channel = NKScriptChannel.getObjectNKScriptChannel(t);
        if (channel == null)
        {
            channel = NKScriptChannel.getObjectNKScriptChannel(t);
            if (channel == null)
                throw new IllegalArgumentException("Cannot find channel for NKScriptExport member " + t.getName());

        }

        String pluginNS = channel.ns;
        int id = channel.getNativeSeq();
        _instanceid = id;
        String ns = String.format(Locale.US, "%s[%d]", pluginNS, id);
        this._channel = channel;

        // super.init(ns: ns, context: channel, origin: nil)
        this.context = context;
        this.namespace = ns;
        this._origin = this;
        // end super init

        channel.addInstance(id, this);
        proxy = bindObject(value);
    } */



    private NKScriptInvocation bindObject(Object obj) {
        nativeObject = obj;
        NKScriptInvocation proxy = new NKScriptInvocation(obj);
        NKScriptValue.setForObject(obj, this);
        return proxy;
    }

    private void unbindObject(Object obj) throws Exception {
        _channel.removeInstance(_instanceid);
        nativeObject = null;
        NKScriptValue.setForObject(obj, null);
        proxy = null;
    }

    void invokeNativeMethod(String method, Object[] args, NKCallback<Object> callback) {
        if (proxy == null)
        {
            return;
        }

        NKScriptTypeInfo.NKScriptTypeInfoMemberInfo member = _channel.typeInfo.item(method);
        if (member != null)
        {
            Method mi = member.getmethod();
            proxy.callAsync(mi, wrapArgs(args), callback);
        }

        callback.onReceiveValue(null);
    }

    Object invokeNativeMethodSync(String method, Object[] args)  {

        if (proxy == null)
        {
            return null;
        }

        NKScriptTypeInfo.NKScriptTypeInfoMemberInfo member = _channel.typeInfo.item(method);
        if (member != null)
        {
            Method mi = member.getmethod();
            return proxy.call(mi, wrapArgs(args));
        }

        return null;

    }

    private Object[] wrapArgs(Object[] args) {

        ArrayList<Object> result = new ArrayList<Object>();

        for (Object obj : args) {
            result.add(wrapScriptObject(obj));
        }

        result.add(this);

        return result.toArray();
    }

    @SuppressWarnings("unchecked")
    private Object wrapScriptObject(Object obj)
    {
        if (obj == null)
            return null;

        if (obj instanceof Map<?,?>)
        {
            Map<String, Object> dict = (Map<String, Object>) obj;

            if (dict.containsKey("$sig") && (Integer.getInteger((String)dict.get("$sig")) == 0x5857574F))
            {
                int num = Integer.getInteger((String)dict.get("$ref"));
                return new NKScriptValue(num, this.context, this);
            }

            if (dict.containsKey("$ns"))
            {
                String ns = (String) dict.get("$ns");
                if (ns != null)
                    return new NKScriptValue(ns, this.context, this);
            }
        }

        return obj;
    }


    // OVERRIDE METHODS IN NKScriptValue

    public void invokeMethod(String method, Object[] args, NKCallback<String> completionHandler) {

        if (proxy == null)
        {
           return;
        }

        NKScriptTypeInfo.NKScriptTypeInfoMemberInfo member = _channel.typeInfo.item(method);
        if (member != null)
        {
            Method mi = member.getmethod();
            proxy.callAsync(mi, wrapArgs(args), completionHandler);
        }
        else
            super.invokeMethod(method, args, completionHandler);
    }

    public void invokeMethod(String method, Object[] arguments) {
        this.invokeMethod(method, arguments, null);
    }


}