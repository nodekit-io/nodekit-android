package co.synchealth.apptest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import java.util.HashMap;

import io.nodekit.nkscripting.NKScriptContext;
import io.nodekit.nkscripting.NKApplication;
import io.nodekit.nkscripting.NKScriptContextFactory;
import io.nodekit.nkscripting.util.NKLogging;

public class MainActivity extends AppCompatActivity implements NKScriptContext.NKScriptContextDelegate {

    private  NKScriptContext context;
    private JavaScriptBridge bridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NKApplication.setAppContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        try {
            NKScriptContextFactory.createContext(null, this);
        }
        catch (Exception e) {
            Log.v("NodeKitAndroid", e.toString());
        }
	
    }

    public void NKScriptEngineDidLoad(NKScriptContext context) {
        NKLogging.log("ScriptEngine Loaded");
        this.context = context;

        bridge = new JavaScriptBridge(this);
        HashMap<String, Object> options = new HashMap<String, Object>();

        try {
            context.loadPlugin(bridge, "myactivity", options);

        } catch (Exception e) {
            Log.e("MYAPP", "exception: " + e.getMessage());
            Log.e("MYAPP", "exception: " + e.toString());
        }

    }

    public void NKScriptEngineReady(NKScriptContext context) {

        NKLogging.log("ScriptEngine Ready");

        bootstrap();
    }

    void bootstrap() {

        try {
            String jsbuf = "myactivity.factorial(10, function(f) { myactivity.updateTitle(f); } ); ";
            context.evaluateJavaScript(jsbuf, null);
        } catch (Exception e) {
            NKLogging.log(e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
