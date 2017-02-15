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

package io.nodekit.nkelectro;

import android.util.Base64;
import android.util.SparseArray;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptValue;
import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;


import android.webkit.MimeTypeMap;

final class NKE_Protocol implements NKScriptExport
{
    static void attachTo(NKScriptContext context, Map<String, Object> appOptions) throws Exception {
        HashMap<String,Object> options = new HashMap<String, Object>();
        options.put("js","lib_electro/protocol.js");
        NKE_Protocol protocol = new NKE_Protocol();
        context.loadPlugin(protocol, "io.nodekit.electro.protocol", options);
        protocol.registerInternalProtocol("app");
    }

    private static HashMap<String, NKScriptValue> registeredSchemes  = new HashMap<>();
    private static SparseArray<NKE_ProtocolCustomRequest> activeRequests  = new SparseArray<>();

    @JavascriptInterface
    public void registerCustomProtocol(String scheme, NKScriptValue handler) {
        scheme = scheme.toLowerCase();
        NKE_Protocol.registeredSchemes.put(scheme, handler);
        NKE_ProtocolCustomHandler nativeHandler = new NKE_ProtocolCustomHandler();
        NKE_WebContents_AndroidWebView.registerScheme(scheme, nativeHandler);
    }

    @JavascriptInterface
    public void registerInternalProtocol(String scheme) {
        scheme = scheme.toLowerCase();
        NKE_ProtocolLocalHandler nativeHandler = new NKE_ProtocolLocalHandler();
        NKE_WebContents_AndroidWebView.registerScheme(scheme, nativeHandler);
    }

    @JavascriptInterface
    public void unregisterCustomProtocol(String scheme) {
        scheme = scheme.toLowerCase();

        if (registeredSchemes.containsKey(scheme))
        {
            registeredSchemes.remove(scheme);
            NKE_WebContents_AndroidWebView.unregisterScheme(scheme);
        }
    }

    @JavascriptInterface
    public void callbackWriteData(int id, Map<String, Object> res) {
        NKE_ProtocolCustomRequest nativeRequest = activeRequests.get(id);
        if (nativeRequest == null) { return; }
        nativeRequest.callbackWriteData(res);
    }

    @JavascriptInterface
    public void callbackEnd(int id, Map<String, Object> res) {
        NKE_ProtocolCustomRequest nativeRequest = activeRequests.get(id);
        if (nativeRequest == null) { return; }
        activeRequests.delete(id);
        nativeRequest.callbackEnd(res);
    }

    @JavascriptInterface
    public void callbackWriteFile(int id, String filename) {
        NKE_ProtocolCustomRequest nativeRequest = activeRequests.get(id);
        if (nativeRequest == null) { return; }
        activeRequests.delete(id);
        nativeRequest.callbackFile(filename);
    }

    @JavascriptInterface
    public Boolean isProtocolHandled(String scheme) {
        return registeredSchemes.containsKey(scheme.toLowerCase());
    }

    private static void _emitRequest(Map<String, Object> req, NKE_ProtocolCustomRequest nativeRequest) {
        String scheme = (String) req.get("scheme");
        int id = nativeRequest.id;

        NKScriptValue handler = registeredSchemes.get(scheme);
        activeRequests.append(id, nativeRequest);

        handler.callWithArguments(new Object[] { req }, null);
    }

    private static int sequenceNumber = 1;

    interface ProtocolHandler {
        WebResourceResponse invoke(Map<String, Object> request);
    };

    private class NKE_ProtocolCustomHandler implements ProtocolHandler {

       public WebResourceResponse invoke(Map<String, Object> req)  {
            String scheme = (String)req.get("scheme");
            if (!registeredSchemes.containsKey(scheme)) { NKLogging.log("Unkown scheme " + scheme); return null; }

            try {

                NKE_ProtocolCustomRequest nativeRequest = new NKE_ProtocolCustomRequest(req);
                NKE_Protocol._emitRequest(req, nativeRequest);
                return new WebResourceResponse(nativeRequest.mimeType, "utf-8", nativeRequest.ins);

            } catch (Exception e) {
                NKLogging.log(e);
            }

            return null;
        }

    }

    private class NKE_ProtocolLocalHandler implements ProtocolHandler {

         public WebResourceResponse invoke(Map<String, Object> req)  {
            String scheme = ((String)req.get("scheme"));
            if (!scheme.equals("app")) { NKLogging.log("Unkown internal " + scheme);  return null; }

            try {

                NKE_ProtocolCustomRequest nativeRequest = new NKE_ProtocolCustomRequest(req);
                nativeRequest.callbackFile("app.nkar" + (String)req.get("path"));
                return new WebResourceResponse(nativeRequest.mimeType, "utf-8", nativeRequest.ins);

            } catch (Exception e) {
                NKLogging.log(e);
            }

            return null;
        }

    }


    private class NKE_ProtocolCustomRequest {

        Boolean isCancelled = false;
        int id;
        PipedOutputStream outs;
        PipedInputStream ins;
        String mimeType;

        NKE_ProtocolCustomRequest(Map<String, Object> req) throws Exception {
            this.id = sequenceNumber++;
            ins = new PipedInputStream();
            outs = new PipedOutputStream(ins);
            mimeType = mimeType((String)req.get("path"));

        }

        private String mimeType(String fname){
            String ext=fname.replaceAll(".*\\.", "");
            mimeType=MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            //  if (mimeType == null) {
            //   TODO    mimeType=ownMimeMap.get(ext);
            //  }
            return mimeType;
        }

        void callbackWriteData(Map<String, Object> res)  {
            String chunk = (String)res.get("_chunk");

            byte[] buffer = Base64.decode(chunk, Base64.DEFAULT);
            try {
                this.outs.write(buffer);

            } catch (Exception e) {
                NKLogging.log(e);
            }
        }

        void callbackFile(String filename) {

            if (this.isCancelled) return;

            InputStream stream = NKStorage.getStream(filename);

            try {
                if (stream != null)
                    NKStorage.copyStream(stream, outs, true);
                else
                    outs.close();
            } catch (Exception e) {
                NKLogging.log(e);
            }

        }

        void callbackEnd(Map<String, Object> res) {

            if (this.isCancelled) return;

            String chunk = (String)res.get("_chunk");

            if (chunk != null) {

                byte[] buffer = Base64.decode(chunk, Base64.DEFAULT);
                try {
                    this.outs.write(buffer);

                } catch (Exception e) {
                    NKLogging.log(e);
                }
            }

            try {
                this.outs.close();

            } catch (Exception e) {
                NKLogging.log(e);
            }

        }

    }

}
