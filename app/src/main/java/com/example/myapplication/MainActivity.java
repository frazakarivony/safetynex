package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private Button button;
    private Button close;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  askForSystemOverlayPermission();

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        close = (Button) findViewById(R.id.close);


        int badge_count = getIntent().getIntExtra("badge_count", 0);

        textView.setText(badge_count + " messages received previously");
        ((MainApp)getApplication()).setCurrentActivity(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !Settings.canDrawOverlays(MainActivity.this)) {

                    //If the draw over permission is not available to open the settings screen
                    //to grant the permission.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
                }else{
                    initializeView();
                }

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finishAffinity();
                ((MainApp)getApplication()).setCurrentActivity(null);
                System.exit(0);
            }
        });
        if( ((MainApp)getApplication()).isFirstRun()) {
            button.callOnClick();
            ((MainApp)getApplication()).setFirsRun(false);
        }
    }

    private void initializeView() {
        startService(new Intent(MainActivity.this, FloatingWidgetService.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        clearReferences();
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        clearReferences();
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();


       /* // To prevent starting the service if the required permission is NOT granted.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //Permission is not available. Display error text.
                Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
                finish();
            }
        }*/
        /*else {
            super.onActivityResult(requestCode, resultCode, data);
        }*/
    }

    private void clearReferences(){
        Activity currActivity = ((MainApp)getApplication()).getCurrentActivity();
        if (this.equals(currActivity))
            ((MainApp)getApplication()).setCurrentActivity(null);
    }
}

