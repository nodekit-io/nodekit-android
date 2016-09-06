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

public class NKEBrowserSwitches   
{
    public NKEBrowserSwitches() {
    }

    // Enable plugins.
    public static final String kEnablePlugins = "enable-plugins";
    // Ppapi Flash path.
    public static final String kPpapiFlashPath = "ppapi-flash-path";
    // Ppapi Flash version.
    public static final String kPpapiFlashVersion = "ppapi-flash-version";
    // Path to client certificate.
    public static final String kClientCertificate = "client-certificate";
    // Disable HTTP cache.
    public static final String kDisableHttpCache = "disable-http-cache";
    // Register schemes to standard.
    public static final String kRegisterStandardSchemes = "register-standard-schemes";
    // Register schemes to handle service worker.
    public static final String kRegisterServiceWorkerSchemes = "register-service-worker-schemes";
    // The minimum SSL/TLS version ("tls1", "tls1.1", or "tls1.2") that
    // TLS fallback will accept.
    public static final String kSSLVersionFallbackMin = "ssl-version-fallback-min";
    // Comma-separated list of SSL cipher suites to disable.
    public static final String kCipherSuiteBlacklist = "cipher-suite-blacklist";
    // The browser process app model ID
    public static final String kAppUserModelId = "app-user-model-id";
    // The command line switch versions of the options.
    public static final String kZoomFactor = "zoom-factor";
    public static final String kPreloadScript = "preload";
    public static final String kPreloadURL = "preload-url";
    public static final String kNodeIntegration = "node-integration";
    public static final String kGuestInstanceID = "guest-instance-id";
    public static final String kOpenerID = "opener-id";
    // Widevine options
    // Path to Widevine CDM binaries.
    public static final String kWidevineCdmPath = "widevine-cdm-path";
    // Widevine CDM version.
    public static final String kWidevineCdmVersion = "widevine-cdm-version";
}


