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

package io.nodekit.nkscripting.util;

import android.util.SparseArray;

import java.util.HashMap;

public class NKEventEmitter   
{
    // static fields and methods

    public static NKEventEmitter global = new NKEventEmitter(true);

    private static int subscriptionSeq = 1;

    // helper classes (internal)

    public interface NKEventSubscription
    {
        void remove() throws Exception ;
    }

    public interface NKHandler<T>
    {
       void invoke(String event, T obj);
    }

    protected abstract class NKHandlerWrapper<T> implements NKHandler<T> {

        private NKHandler<T> _base;

        NKHandlerWrapper(NKHandler<T> base) {
            _base = base;
        }

        abstract void call(String event, T obj);

        public void invoke(String event, T obj){
            call(event, obj);
            _base.invoke(event, obj);
        }

     }

    protected class NKEventSubscriptionGeneric<T>  implements NKEventSubscription
    {
        public void remove()  {
            emitter.subscriptions.get(eventType).remove(id);
        }

        private NKEventEmitter emitter;
        private String eventType;
        public int id;

        public NKHandler<T> handler;

        public NKEventSubscriptionGeneric(NKEventEmitter emitter, String eventType, NKHandler<T> handler)  {
            id = subscriptionSeq++;
            this.eventType = eventType;
            this.emitter = emitter;
            this.handler = handler;
        }
    }

    // instance fields

    protected Boolean signalEmitter;

    protected HashMap<String, Object> earlyTriggers;

    protected NKEventSubscription currentSubscription;

    protected HashMap<String, SparseArray<NKEventSubscription>> subscriptions = new HashMap<String, SparseArray<NKEventSubscription>>();

    // constructors

    public NKEventEmitter(Boolean isSignalEmitter)
    {
        signalEmitter = isSignalEmitter;
        if (isSignalEmitter)
            earlyTriggers = new HashMap<String, Object>();
    }

    public NKEventEmitter()
    {
        this(false);
    }

    // instance methods

    public <T>NKEventSubscription on(String eventType, NKHandler<T> handler)  {

        SparseArray< NKEventSubscription> eventSubscriptions = new SparseArray< NKEventSubscription>();

        if (subscriptions.containsKey(eventType))

            eventSubscriptions = subscriptions.get(eventType);

        else

            eventSubscriptions = new SparseArray< NKEventSubscription>();

        NKEventSubscriptionGeneric<T> subscription = new NKEventSubscriptionGeneric<T>(this, eventType, handler);

        eventSubscriptions.put(subscription.id, subscription);

        subscriptions.put(eventType, eventSubscriptions);

        return subscription;

    }


    @SuppressWarnings("unchecked")
    public <T>void once(String eventType, NKHandler<T> handler)  {

        if (signalEmitter && earlyTriggers.containsKey(eventType))
        {
            Object data = earlyTriggers.get(eventType);
            earlyTriggers.remove(eventType);
            handler.invoke(eventType, (T)data);
            return ;
        }

        NKHandlerWrapper<T> canceler = new NKHandlerWrapper<T>(handler) {
            void call(String event, T obj) {
                try {
                    currentSubscription.remove();
                } catch (Exception e) {
                    // ignore
                }
            }
        };

        this.on(eventType, canceler);
    }

    @SuppressWarnings("unchecked")
    public <T>void emit(String eventType, T data)  {
        this.emit(eventType, data, false);
    }

    @SuppressWarnings("unchecked")
    public <T>void emit(String eventType, T data, Boolean forward)  {

        if (subscriptions.containsKey(eventType))
        {

            SparseArray<NKEventSubscription> sparseArray = subscriptions.get(eventType);

            for(int i = 0; i < sparseArray.size(); i++) {
                int key = sparseArray.keyAt(i);
                // get the object by the key.
                NKEventSubscription item = sparseArray.get(key);
                currentSubscription = item;
                ((NKEventSubscriptionGeneric<T>)item).handler.invoke(eventType, data);
            }

        }  else
        {
            if (signalEmitter)
                earlyTriggers.put(eventType, data);
        }
    }


    public void removeAllListeners(String eventType) {
        if (eventType != null)
            subscriptions.remove(eventType);
        else
            subscriptions.clear();
    }



}


