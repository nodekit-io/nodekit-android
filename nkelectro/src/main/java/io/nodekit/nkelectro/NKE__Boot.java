package io.nodekit.nkelectro;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.webkit.JavascriptInterface;

import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.util.NKEventEmitter;
import io.nodekit.nkscripting.util.NKEventHandler;
import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKScriptValue;
import java.util.HashMap;
import java.util.Map;

import io.nodekit.nkscripting.util.NKLogging;
import io.nodekit.nkscripting.util.NKStorage;

public class NKE__Boot {

    public static void addRendererElectro(NKScriptContext context, Map<String, Object> options) throws Exception
    {
        String appjs = NKStorage.getResource("lib_electro/_nke_renderer.js");
        String script = "function loadbootstrap(){\n" + appjs + "\n}\n" + "loadbootstrap();" + "\n";
        NKScriptSource scriptsource = new NKScriptSource(script, "io.nodekit.electro/lib-electro/_nke_renderer.js", "io.nodekit.electro.renderer", null);
        context.injectJavaScript(scriptsource);

        HashMap<String, Object> optionsDefault = new HashMap<String, Object>();

        NKE_IpcRenderer.attachTo(context, optionsDefault);
    }

}
