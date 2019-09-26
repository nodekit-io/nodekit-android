package co.synchealth.apptest;

import android.app.Activity;
import android.widget.TextView;
import android.webkit.JavascriptInterface;

import io.nodekit.engine.JSContext;

public class JavaScriptBridge {

    private Activity mainActivity;

    JavaScriptBridge(Activity main) {
        this.mainActivity = main;
    }

    @JavascriptInterface
    public Integer factorial(Integer x) {
        int factorial = 1;
        for (; x > 1; x--) {
            factorial *= x;
        }
        return factorial;
    }

    @JavascriptInterface
    public void updateTitle(Integer result) {

        TextView textView = mainActivity.findViewById(R.id.textView);
        textView.setText(result + "");

    }

}
