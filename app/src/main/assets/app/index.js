/**
 * Copyright (c) 2016-9 OffGrid Networks. All Rights Reserved.
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

console.log("STARTING SAMPLE APPLICATION");

const secondFunction = require("./subdirectory/index")

secondFunction()

var last = now()
var counter = 0
/*var timerId = setInterval(function() {
    const newTime = now()
    console.error("delta :" + (newTime - last))
    last = newTime
    counter += 1
    if (counter == 5) {
        clearTimeout(timerId)
    }
}, 2500)*/

setTimeout(function() {
    console.log("single timer fire")
    setTimeout(function() {
        console.log("second timer fire")
    }, 2000)
}, 15000)

console.log("variadic", "args", "test", {}, new Date())
console.warn("variadic", "args", "test")
console.error("variadic", "args", "test")
console.info("variadic", "args", "test")
console.dir("variadic", "args", "test")

function now() {
  return new Date().getTime()
}