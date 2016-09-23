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

import java.lang.reflect.*;
import java.util.*;
import android.webkit.JavascriptInterface;
import io.nodekit.nkscripting.util.NKCallback;
import io.nodekit.nkscripting.util.NKLogging;


class NKScriptTypeInfo<T> {

    private Class<T> _pluginType;
    private T _instance;
    private Map<String, NKScriptTypeInfoMemberInfo> _members;

    NKScriptTypeInfo(Class<T> pluginType)  {

        _pluginType = pluginType;
        try {
            Constructor<T> ctor = _pluginType.getConstructor();
            _instance= ctor.newInstance();
        } catch (Exception e) {
            NKLogging.log(e);
            _instance = null;
        }

        this.reflectMethods();

    }

    NKScriptTypeInfo(T instance, Class<T> pluginType)  {

        _pluginType = pluginType;
        _instance = instance;

        this.reflectMethods();

    }


    private void reflectMethods() {

        _members = new HashMap<String, NKScriptTypeInfoMemberInfo>();

        for (Constructor c : _pluginType.getDeclaredConstructors()) {
            int modifiers = c.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {

                {
                    NKScriptTypeInfoMemberInfo member = new NKScriptTypeInfoMemberInfo(c);
                    _members.put(member.key, member);
                }
            }
        }

        for (Method m : _pluginType.getDeclaredMethods()) {
            int modifiers = m.getModifiers();
            if (m.isAnnotationPresent(JavascriptInterface.class) && Modifier.isPublic(modifiers)) {

                {
                    NKScriptTypeInfoMemberInfo member = new NKScriptTypeInfoMemberInfo(m);
                    _members.put(member.name, member);
                }
            }
        }
    }

    Class getType()  {
        return _pluginType;
    }

    NKScriptTypeInfoMemberInfo item(String item) {
        return _members.get(item);
    }

    Collection<NKScriptTypeInfoMemberInfo> getitems() {
        return _members.values();
    }

    NKScriptTypeInfoMemberInfo defaultConstructor() {
        return _members.get("");
    }

    boolean containsMethod(String item) {
        return _members.containsKey(item);
    }

    boolean containsConstructor(String item)  {
        return _members.containsKey(item);
    }

    static String[] instanceMethods(Class clazz) {
        List<String> result = new ArrayList<String>();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                    result.add(method.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result.toArray(new String[result.size()]);
    }

    private enum MemberType
    {
        Method,
        Constructor
    }

    class NKScriptTypeInfoMemberInfo
    {

        NKScriptTypeInfoMemberInfo(Constructor constructor)  {
            _memberType = MemberType.Constructor;
            _constructor = constructor;

            name = constructor.getName();
            isVoid = true;
            isAsyncCallback = false;

            Class[] params = constructor.getParameterTypes();
            arity = params.length;

            StringBuilder sb = new StringBuilder();
            Boolean started = false;
            int argno = 0;
            for (Class param : params) {
                if (!started)
                {
                    started = true;
                } else
                {
                    sb.append(":");
                }
                sb.append("arg" + argno++ + param.getSimpleName().toLowerCase());

            }
            key = sb.toString();

        }

        NKScriptTypeInfoMemberInfo(Method method) {

            _memberType = MemberType.Method;
            _method = method;

            name = method.getName();
            isAsyncCallback = false;

            Class[] params = method.getParameterTypes();

            arity = params.length;
            isVoid = (_method.getReturnType().equals(Void.TYPE));

            StringBuilder sb = new StringBuilder();
            int argno = 0;
            Boolean started = false;
            for (Class param : params) {
                if (!started)
                {
                    started = true;
                } else
                {
                    sb.append(":");
                }

                sb.append("arg" + argno++ + param.getSimpleName().toLowerCase());

                if (param == NKCallback.class)
                {
                    isAsyncCallback = true;
                }
            }
            key = sb.toString();

        }

        int arity;
        boolean isVoid;
        boolean isAsyncCallback;
        String name;
        String key;

        private MemberType _memberType;
        private Method _method;
        private Constructor _constructor;

        boolean isMethod() {
            return (_memberType == MemberType.Method);
        }

        boolean isConstructor() {
            return (_memberType == MemberType.Constructor);
        }

        Method getmethod() {
            return _method;
        }

        Constructor getconstructor() {
            return _constructor;
        }

        String getNKScriptingjsType() {
            int _arity = arity;
            switch(this._memberType)
            {
                case Method:
                    break;
                case Constructor:
                    break;
                default:
                    arity = -1;
                    break;

            }
            if (isVoid && (_arity < 0))
                return "";
            else
                return "#" + (_arity >= 0 ? Integer.toString(_arity) : "") + (isVoid ? "a" : "s");
        }

    }

}
