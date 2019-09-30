package co.synchealth.apptest;

import android.app.Activity;
import android.widget.TextView;
import android.webkit.JavascriptInterface;

import java.util.List;

import io.nodekit.nkscripting.NKScriptExport;
import io.nodekit.nkscripting.NKScriptValue;

public class JavaScriptBridge implements NKScriptExport {

    private Activity mainActivity;

    JavaScriptBridge(Activity main) {
        this.mainActivity = main;
    }

    @JavascriptInterface
    public void factorial(final int start, final NKScriptValue callback) {

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int x = start;
                int factorial = 1;
                for (; x > 1; x--) {
                    factorial *= x;
                }
                callback.callWithArguments(new Object[] { factorial }, null);

            }
        });

    }

    @JavascriptInterface
    public void updateTitle(final int result) {

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = mainActivity.findViewById(R.id.textView);
                textView.setText( Integer.toString(result));
            }
        });

    }

}
