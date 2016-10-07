package io.nodekit.nkelectro;

import io.nodekit.nkscripting.NKScriptSource;
import io.nodekit.nkscripting.NKScriptContext;
import java.util.HashMap;
import java.util.Map;
import io.nodekit.nkscripting.util.NKStorage;

public class NKElectro {

    public static void addToContext(NKScriptContext context, Map<String, Object> options) throws Exception
    {
        String appjs = NKStorage.getResource("lib_electro/_nke_main.js");
        String script = "function loadbootstrap(){\n" + appjs + "\n}\n" + "loadbootstrap();" + "\n";
        NKScriptSource scriptsource = new NKScriptSource(script, "io.nodekit.electro/lib-electro/_nke_main.js", "io.nodekit.electro.main", null);
        context.injectJavaScript(scriptsource);

        NKE_App.attachTo(context, options);
        NKE_BrowserWindow.attachTo(context, options);
        NKE_WebContents_AndroidWebView.attachTo(context, options);
        //  NKE_Dialog.attachTo(context, options);
        NKE_IpcMain.attachTo(context, options);
        NKE_Protocol.attachTo(context, options);
    }


    static void addToRendererContext(NKScriptContext context, Map<String, Object> options) throws Exception
    {
        String appjs = NKStorage.getResource("lib_electro/_nke_renderer.js");
        String script = "function loadbootstrap(){\n" + appjs + "\n}\n" + "loadbootstrap();" + "\n";
        NKScriptSource scriptsource = new NKScriptSource(script, "io.nodekit.electro/lib-electro/_nke_renderer.js", "io.nodekit.electro.renderer", null);
        context.injectJavaScript(scriptsource);

        HashMap<String, Object> optionsDefault = new HashMap<String, Object>();

     //   NKE_IpcRenderer.attachTo(context, optionsDefault);
    }

}
