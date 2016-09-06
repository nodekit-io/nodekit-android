/*
 * nodekit.io
 *
 * Copyright (c) 2016 OffGrid Networks. All Rights Reserved.
 * Portions Copyright 2015 XWebView
 * Portions Copyright (c) 2014 Intel Corporation.  All rights reserved.
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

var exports;

var NKScripting = (function NKScriptingRunOnce(exports) {
    var global = this;
    
    this.Blob = (typeof Blob === 'undefined') ? {} : Blob;
    this.File = (typeof File === 'undefined') ? {} : File;
    this.FileList = (typeof FileList === 'undefined') ? {} : FileList;
    this.ImageData = (typeof ImageData === 'undefined') ? {} : ImageData;
    this.MessagePort = (typeof MessagePort === 'undefined') ? {} : MessagePort;
                   var syncRef = 0;

    var NKScripting = function NKScriptingObject(channelName) {
        this.events = {}
    }
 
    exports = NKScripting;

    NKScripting.createNamespace = function(namespace, object) {
        function callback(p, c, i, a) {
            if (i < a.length - 1)
                return (p[c] = p[c] || {});
            if (p[c] instanceof NKScripting)
                p[c].dispose();
            return (p[c] = object || {});
        }
        return namespace.split('.').reduce(callback, global);

    }

      NKScripting.createPlugin = function(namespace, base) {
             if (base instanceof Object) {
                 // Plugin is a mixin object which contains both JavaScript and native methods/properties.
                 var properties = {};
                 Object.getOwnPropertyNames(NKScripting.prototype).forEach(function(p) {
                     properties[p] = Object.getOwnPropertyDescriptor(this, p);
                 }, NKScripting.prototype);
                 base.__proto__ = Object.create(Object.getPrototypeOf(base), properties);
                 NKScripting.call(base, channelName);
              } else {
                throw new Error("NOT A VALID JavascriptInterface Object");
             }
             return NKScripting.createNamespace(namespace, base);
         }
                   
    /* Polyfill indexOf. */
    var indexOf;

    if (typeof Array.prototype.indexOf === 'function') {
        indexOf = function (haystack, needle) {
            return haystack.indexOf(needle);
        };
    } else {
        indexOf = function (haystack, needle) {
            var i = 0, length = haystack.length, idx = -1, found = false;

            while (i < length && !found) {
                if (haystack[i] === needle) {
                    idx = i;
                    found = true;
                }

                i++;
            }

            return idx;
        };
    };
    
    NKScripting.prototype.on = function (event, listener) {
        if (typeof this.events[event] !== 'object') {
            this.events[event] = [];
        }

        this.events[event].push(listener);
    };

    NKScripting.prototype.removeListener = function (event, listener) {
        var idx;

        if (typeof this.events[event] === 'object') {
            idx = indexOf(this.events[event], listener);

            if (idx > -1) {
                this.events[event].splice(idx, 1);
            }
        }
    };

    NKScripting.prototype.emit = function (event) {
        var i, listeners, length, args = [].slice.call(arguments, 1);
               
        if (typeof this.events[event] === 'object') {
            listeners = this.events[event].slice();
            length = listeners.length;

            for (i = 0; i < length; i++) {
                listeners[i].apply(this, args);
            }
        }
    };

    NKScripting.prototype.once = function (event, listener) {
        this.on(event, function g () {
            this.removeListener(event, g);
            listener.apply(this, arguments);
        });
    };
                   
    /* Polyfill JSON Date Parsing */
                   if (JSON && !JSON.dateParser) {
                   var reISO = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*))(?:Z|(\+|-)([\d|:]*))?$/;
                   JSON.dateParser = function (key, value) {
                   if (typeof value === 'string') {
                   var a = reISO.exec(value);
                   if (a) return new Date(value);
                   }
                   return value;
                   };
                   
                   }
                   
                   
    return exports;
                   
})(exports);