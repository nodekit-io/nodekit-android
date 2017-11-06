/**
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

const nodekit = require('electro').app,
      BrowserWindow = require('electro').BrowserWindow,
      protocol = require('electro').protocol;

console.log("STARTING SAMPLE ELECTRO APPLICATION");

protocol.interceptInternalProtocol("internal");

const functionExport = require("./subdirectory")

functionExport()

const secondFunction = require("./subdirectory/index")

secondFunction()

setInterval(function() {
    console.log("recurring timer fire")
}, 2500)

setTimeout(function() {
    console.log("single timer fire")
    setTimeout(function() {
        console.log("second timer fire")
    }, 2000)
}, 15000)

nodekit.on("ready", function() {

           var p = new BrowserWindow({ 'preloadURL': 'internal://localhost/app/index.html',
                                     'nk.allowCustomProtocol': true,
                                     'nk.taskBarPopup': true,
                                     'nk.taskBarIcon': 'MenuIcon',
                                     'width': 300,
                                     'height': 600
                                     });

           console.log("Server running");
     });

