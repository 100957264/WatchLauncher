package android.fise.com.fiseassitant;


import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class ShutdownRebootHelperActivity extends Activity {
    Button rebootButton;
    Button shutdownButton;
    Button listenButton;
    Button installApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.battery_level);
        setContentView(R.layout.activity_shutdown_reboot_helper);
        /*rebootButton = (Button)findViewById(R.id.button_reboot);

        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rebootIntent = new Intent();
                rebootIntent.setAction("com.android.fise.ACTION_REBOOT");
                sendBroadcast(rebootIntent);
            }
        });
        shutdownButton = (Button) findViewById(R.id.button_shutdown);
        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shutdownIntent = new Intent();
                shutdownIntent.setAction("com.android.fise.ACTION_SHUTDOWN");
                sendBroadcast(shutdownIntent);
            }
        });
        listenButton = (Button)findViewById(R.id.listen_button);
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listenIntent = new Intent();
                listenIntent.setAction("net.wecare.watch_launcher.ACTION_LISTEN");
                sendBroadcast(listenIntent);
                Log.d("fengqing","listenIntent has been sent successfull");
            }
        });
        installApp = (Button)findViewById(R.id.install_app);
        installApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent installIntent = new Intent();
                installIntent.setAction("net.wecare.watch_launcher.ACTION_APP_INSTALL");
                
                sendBroadcast(installIntent);
                Log.d("fengqing","Install App has been sent successfull");
            }
        });*/
    }
}
