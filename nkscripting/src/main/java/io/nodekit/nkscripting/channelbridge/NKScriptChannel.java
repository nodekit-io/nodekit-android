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

import io.nodekit.nkscripting.*;
import io.nodekit.nkscripting.util.*;
import io.nodekit.nkscripting.channelbridge.NKScriptTypeInfo.NKScriptTypeInfoMemberInfo;
import java.util.*;
import android.util.SparseArray;

public class NKScriptChannel implements NKScriptMessage.Handler {

    private String id;
    private Boolean isFactory = false;
    private Boolean isRemote = false;
    private NKScriptValueNative _principal;

    private static HashMap<String, NKScriptChannel> _channels = new HashMap<String, NKScriptChannel>();

    private SparseArray<NKScriptValueNative> _instances = new SparseArray<NKScriptValueNative>();
    private static SparseArray<NKScriptChannel> _instanceChannels = new SparseArray<NKScriptChannel>();

    // Internal variables and helpers
    private static int nativeFirstSequence = java.lang.Integer.MAX_VALUE;
    private static int sequenceNumber = 0;

    // Public properties
    public NKScriptContext context;
    public String ns;
    public NKScriptTypeInfo typeInfo;


    // Public constructors
    public NKScriptChannel(NKScriptContext context) throws Exception
    {
        this.context = context;
    }

    // Public methods
    public <T> NKScriptValue bindPluginClass(Class<T> pluginType, String namespace, HashMap<String, Object> options) throws Exception {

        setObjectNKScriptChannel(context, this);

        if ((this.id != null) || (context == null) ) return null;

        this.ns = namespace;
        this.id = Integer.toString(NKScriptChannel.sequenceNumber++);
        this.isFactory = true;

        ((NKScriptMessage.Controller)context).addScriptMessageHandler(this, id);

        // Class, not instance, passed to bindPlugin -- to be used in Factory constructor/instance pattern in js
        String name = pluginType.getName();

         typeInfo = new NKScriptTypeInfo<T>(pluginType);


        // Need to store the channel on the class itself so it can be found when native construct requests come in from other plugins
        setObjectNKScriptChannel(pluginType, this);

        _principal = new NKScriptValueNative(this.ns, this, 0, pluginType);

        this._instances.put(0, _principal);
        _channels.put(this.ns, this);
        NKScriptValue.setForObject(pluginType, _principal);

        NKScriptExport.Proxy export = new NKScriptExport.Proxy(pluginType);

        NKScriptSource script = new NKScriptSource(_generateStubs(export, name), ns + "/plugin/" + name + ".js");
        context.injectJavaScript(script);
        return _principal;
    }


        // Public methods
    @SuppressWarnings("unchecked")
    public <T> NKScriptValue bindPlugin(T plugin, String namespace, HashMap<String, Object> options)  throws Exception {

        setObjectNKScriptChannel(context, this);

        if ((this.id != null) || (context == null) ) return null;

        this.id = Integer.toString(NKScriptChannel.sequenceNumber++);
        this.ns = namespace;
        this.isFactory = false;

        ((NKScriptMessage.Controller)context).addScriptMessageHandler(this, id);

        // Instance of Princpal passed to bindPlugin -- to be used in singleton/static pattern in js
        Class<T> pluginType = (Class<T>)plugin.getClass();
        String name = pluginType.getName();

         typeInfo = new NKScriptTypeInfo<T>(plugin, pluginType);

        _principal = new NKScriptValueNative(this.ns, this, 0, plugin);

        this._instances.put(0, _principal);
        _channels.put(this.ns, this);
        NKScriptValue.setForObject(plugin, _principal);

        NKScriptExport.Proxy export = new NKScriptExport.Proxy(pluginType);

        NKScriptSource script = new NKScriptSource(_generateStubs(export, name), ns + "/plugin/" + name + ".js");
        context.injectJavaScript(script);
        return _principal;
    }

    public static NKScriptChannel getChannel(String ns)
    {
        return _channels.get(ns);
    }

    public int getNativeSeq()
    {
        int id = NKScriptChannel.nativeFirstSequence--;
        _instanceChannels.put(id, this);
        return id;
    }

    public static NKScriptChannel getNative(int id)
    {
       return _instanceChannels.get(id);
    }

    public void addInstance(int id, NKScriptValueNative instance) {
        _instances.put(id, instance);
    }

    public void removeInstance(int id) {
        _instances.delete(id);

      _instanceChannels.delete(id);

        if (!isFactory)
            NKEventEmitter.global.emit("NKS.SingleInstanceComplete", this.ns);

    }

    private void unbind()
    {
        if (_channels.containsKey(ns))
            _channels.remove(ns);

        if (id == null) return;

        id = null;

        _instances.clear();

        if (isFactory)
            NKScriptChannel.setObjectNKScriptChannel(_principal.nativeObject, null);

        _principal = null;

        try {
            ((NKScriptMessage.Controller)context).removeScriptMessageHandlerForName(id);
        } catch (Exception e) {
            NKLogging.log(e);
        }


        typeInfo = null;
        _instances = null;
        context = null;
    }

    @SuppressWarnings("unchecked")
    public void didReceiveScriptMessage(NKScriptMessage message)
    {
        // A workaround for when postMessage(undefined)
        if (message.body == null) return;

        // thread static
        NKScriptValue.setCurrentContext(this.context);
        if (message.body instanceof Map<?,?>) {
            Map<String, Object> body = (Map<String, Object>) message.body;
            if (body.containsKey("$opcode")) {
                String opcode = (String) body.get("$opcode");
                int target = Integer.parseInt(body.get("$target").toString());
                if (_instances.indexOfKey(target) >= 0) {
                    NKScriptValueNative obj = _instances.get(target);
                    if (opcode.equals("-")) {
                        if (target == 0) {
                            // Dispose plugin
                            this.unbind();
                        } else {
                    //        setObjectNKScriptValue(obj, null);
                            _instances.remove(target);
                        }
                    } else if (typeInfo.containsMethod(opcode)) {
                        // Invoke method
                        // TODO:  ASYNC RESULT
                        obj.invokeNativeMethod(opcode, (Object[]) body.get("$operand"), null);
                    } else {
                        NKLogging.log(String.format("!Invalid member name: %s", opcode));
                    }
                } else if (opcode.equals("+")) {
                    // Create instance
                    Object[] args = (Object[]) body.get("$operand");
                    String nsInstance = String.format(Locale.US, "%s[%d]", this.ns, target);
                    _instances.put(target, new NKScriptValueNative(nsInstance, this, target, args, true));
                } else {
                    // else Unknown opcode
                    if (NKScriptMessage.Handler.class.isAssignableFrom(_principal.nativeObject.getClass())) {
                        NKScriptMessage.Handler obj = (NKScriptMessage.Handler) _principal.nativeObject;
                        obj.didReceiveScriptMessage(message);
                    } else {
                        // discard unknown message
                        NKLogging.log(String.format("!Unknown message: %s", message.body.toString()));
                    }
                }
            }
        }

        //thread static
        NKScriptValue.unsetCurrentContext();
    }

    @SuppressWarnings("unchecked")
    public Object didReceiveScriptMessageSync(NKScriptMessage message)
    {
        // A workaround for when postMessage(undefined)
        if (message.body == null) return false;

        // thread static
        NKScriptValue.setCurrentContext(this.context);
        Object result = false;

        if (message.body instanceof Map<?,?>)
        {
            Map<String, Object> body = (Map<String, Object>) message.body;
            if (body.containsKey("$opcode"))
            {
                String opcode = (String) body.get("$opcode");
                int target = Integer.parseInt(body.get("$target").toString());
                if (_instances.indexOfKey(target) >= 0)
                {
                    NKScriptValueNative obj = _instances.get(target);
                    if (opcode.equals("-"))
                    {
                        if (target == 0)
                        {
                            // Dispose plugin
                            this.unbind();
                            result = true;
                        }
                        else if (_instances.indexOfKey(target) >=0 )
                        {
                            setObjectNKScriptChannel(obj, null);
                            result = true;
                        }
                        else
                        {
                            NKLogging.log(String.format("!Invalid instance id: %s", target));
                            result = false;
                        }
                    }
                    else if (typeInfo.containsMethod(opcode))
                    {
                        // Invoke method
                        result = obj.invokeNativeMethodSync(opcode, (Object[])body.get("$operand"));
                    }
                    else {
                        NKLogging.log(String.format("!Invalid member name: %s", opcode));
                        result = false;
                    }
                }
                else if (opcode.equals("+"))
                {
                    // Create instance
                    Object[] args = (Object[]) body.get("$operand");
                    String nsInstance = String.format(Locale.US, "%s[%d]", this.ns, target);
                    _instances.put(target, new NKScriptValueNative(nsInstance, this, target, args, true));
                    result = true;
                }
                else
                {
                    // else Unknown opcode
                    if (NKScriptMessage.Handler.class.isAssignableFrom(_principal.nativeObject.getClass())) {
                        NKScriptMessage.Handler obj = (NKScriptMessage.Handler) _principal.nativeObject;
                        result = obj.didReceiveScriptMessageSync(message);
                    }
                    else
                    {
                        // discard unknown message
                        NKLogging.log(String.format("!Unknown message: %s", message.body.toString()));
                        result = false;
                    }
                }
            }

        }

        //thread static
        NKScriptValue.unsetCurrentContext();
        return result;
    }


    private String _generateMethod(String key, String item, Boolean prebind)
    {
        String stub = String.format("NKScripting.invokeNative.bind(%s, '%s')", item, key);
        return prebind ? String.format("%s;", stub) : "function(){return " + stub + ".apply(null, arguments);}";
    }

    private String _generateStubs(NKScriptExport.Proxy export, String name)
    {
        Boolean prebind = !this.isFactory;
        StringBuilder stubs = new StringBuilder();

        for (Object obj : typeInfo.getitems())
        {
            NKScriptTypeInfoMemberInfo member = (NKScriptTypeInfoMemberInfo) obj;

            String stub;
            if ((member.isMethod()) && (!member.name.equals("")))
            {
                String methodStr = _generateMethod(String.format("%s%s", member.key, member.getNKScriptingjsType()), prebind ? "exports" : "this", prebind);
                if (member.isAsyncCallback)
                {
                    stub = String.format("exports.%s = %s", member.name + "Sync", methodStr);
                    stubs.append(export.rewriteGeneratedStub(stub, member.name + "Sync")).append("\n");

                    stub = String.format("exports.%s = %s", member.name + "Async", methodStr);
                    stubs.append(export.rewriteGeneratedStub(stub, member.name + "Async")).append("\n");
                }
                else
                {
                    stub = String.format("exports.%s = %s", member.name, methodStr);
                    stubs.append(export.rewriteGeneratedStub(stub, member.name)).append("\n");
                }
            }
        }

        String basestub;
        if (this.isFactory)
        {
            NKScriptTypeInfoMemberInfo constructor = typeInfo.defaultConstructor();
            // basestub = generateMethod("\(member.type)", this: "arguments.callee", prebind: false)
            basestub = export.rewriteGeneratedStub(String.format("'%s'", constructor.getNKScriptingjsType()), ".base");
        } else
        {
            basestub = export.rewriteGeneratedStub("null", ".base");
        }

        String localstub = export.rewriteGeneratedStub(stubs.toString(), ".local");
        String globalstubber = "(function(exports) {\n" + localstub + "})(NKScripting.createPlugin('" + id + "', '" + this.ns + "', " + basestub + "));\n";

        NKLogging.log(globalstubber);
        return export.rewriteGeneratedStub( globalstubber, ".global");
    }

    // STATIC METHODS FOR ANY OBJECT
    private static HashMap<Object, NKScriptChannel> objScriptChannel = new HashMap<Object, NKScriptChannel>();
    private static HashMap<Object, NKScriptValue> objScriptValue = new HashMap<Object, NKScriptValue>();

    public static NKScriptChannel getObjectNKScriptChannel(Object obj)
    {
        return objScriptChannel.containsKey(obj) ? objScriptChannel.get(obj) : null;
    }

    static void setObjectNKScriptChannel(Object obj, NKScriptChannel value)
    {
        if (value != null)
            objScriptChannel.put(obj, value);
        else
            objScriptChannel.remove(obj);
    }

}

