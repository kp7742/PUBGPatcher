package com.kmods.pubgpatcher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        }

        if(!getIntent().getStringExtra("CURRVER").equals("v" + BuildConfig.VERSION_NAME)){
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle("New Update Found!");
            ab.setMessage("New Version PUBGPatcher " + getIntent().getStringExtra("CURRVER") + " Available To Download.");
            ab.setPositiveButton("Download", (dialog, which) -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://kmods.ml/"));
                startActivity(i);
            });
            ab.setNegativeButton("Exit", (dialog, which) -> {
                MainActivity.this.finish();
            });
            ab.setCancelable(false);
            ab.show();
        }

        TextView exStatus = findViewById(R.id.expirystatus);
        String subtime = "Subscription Time: " + timeConvert(getIntent().getIntExtra("EXPIRY", 0));
        exStatus.setText(subtime);

        Button mStart = findViewById(R.id.startbtn);
        mStart.setOnClickListener(v -> {
            startPatcher();
        });
        Button mStop = findViewById(R.id.stopbtn);
        mStop.setOnClickListener(v -> {
            stopService(new Intent(this, Floater.class));
        });
    }

    private String timeConvert(int time){
        StringBuilder sb = new StringBuilder();
        int day = (time/(24*60));
        int hour = ((time%(24*60)) / 60);
        int mint = ((time%(24*60)) % 60);
        if(day > 0){
            sb.append(day).append("D ");
        }
        if(hour > 0){
            sb.append(hour).append("H ");
        }
        sb.append(mint).append("M");
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 123) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Please allow this permission, so " + getString(R.string.app_name) + " could be drawn.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (Floater.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    void startPatcher() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        } else {
            startFloater();
        }
    }

    private void startFloater() {
        if (!isServiceRunning()) {
            startService(new Intent(this, Floater.class));
        } else {
            Toast.makeText(this, "Service Already Running!", Toast.LENGTH_SHORT).show();
        }
    }
}
