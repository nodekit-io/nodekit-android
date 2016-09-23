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
import java.util.Locale;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKCallback;

class NKScriptInvocation
{
    final Object target;
    final Class targetClass;

    NKScriptInvocation(Object obj) {

        Class t = obj.getClass();
        if (t == Class.class)
        {
            targetClass = obj instanceof Class ? (Class)obj : (Class)null;
            target = null;
        } else
        {
            target = obj;
            targetClass = t;
        }

    }

    static Object construct(Class target, Constructor constructor, Object[] args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e)
        {
            NKLogging.log(e);
            return null;
        }

    }

    Object call(Method method, Object[] args)  {
        try {
           return method.invoke(target, args);
            // return method.invoke(target, unwrapArgs(method, args));
        } catch (Exception e)
        {
            NKLogging.log(e);
            return null;
        }
    }

    void callAsync(Method method, Object[] args, NKCallback callback)  {
        try {

            method.invoke(target, args, callback);
            //  method.invoke(target, unwrapArgs(method, args), callback);
        } catch (Exception e)
        {
            NKLogging.log(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected Object[] unwrapArgs(Method m, Object[] args) {

        Class[] paramInfos = m.getParameterTypes();

        if (args != null && args.length > paramInfos.length) {
            NKLogging.log(String.format(Locale.US, "Too many js arguments passed to plugin method %s;  expected %d} got %d", m.getName(), paramInfos.length, args.length));
            return null;
        }

        Object[] newArgs = new Object[paramInfos.length];
        int k = 0;
        for (int i = 0;i < paramInfos.length;i++)
        {
            newArgs[i] = paramInfos[i].cast(args[i]);
        }

        return newArgs;
    }

}


