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

this.process = this.process || {}
var process = this.process;

process.platform = process.platform || "android"
process.type = "main"
process.versions = {}

this.console = this.console || function () { };

console.log = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Info", {})
};
console.log.debug = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Debug", {})
};
console.log.info = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Info", {})
};
console.log.notice = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Notice", {})
};
console.log.warning = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Warning", {})
};
console.log.error = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Error", {})
};
console.log.critical = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Critical", {})
};
console.log.emergency = function () {
  const msg = Array.prototype.slice.call(arguments).join(" ");
  NKScriptingBridge.log(msg, "Emergency", {})
};
console.warn = console.log.warning;
console.error = console.log.error;
console.info = console.log.info;
console.dir = console.log.debug;

(function (con) {
    var prop, method;
    var empty = {};
    var dummy = function () { };
    var properties = "memory".split(",");
    var methods = ("assert,count,debug,dir,dirxml,error,exception,group,"
        + "groupCollapsed,groupEnd,info,log,markTimeline,profile,profileEnd,"
        + "time,timeEnd,trace,warn").split(",");
    while (prop = properties.pop()) {
        con[prop] = con[prop] || empty;
    }
    while (method = methods.pop()) {
        con[method] = con[method] || dummy;
    }
})(console);

NKScripting.serialize = true;

NKScripting.getMessageHandlers = function (name) {

    return {
        'postMessage': function (message) { NKScriptingBridge.didReceiveScriptMessage(name, JSON.stringify(message)); },
        'postMessageSync': function (message) { return NKScriptingBridge.didReceiveScriptMessageSync(name, JSON.stringify(message)) }
    };
}

console.log("NKNodeKit WebView: Android JavaScript Engine Initialized");