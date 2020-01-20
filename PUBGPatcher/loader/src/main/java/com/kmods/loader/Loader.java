package com.kmods.loader;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Loader {
    private String pkg;
    private String daemonPath;

    private static boolean isRunning = false;

    private Service floater;
    private Context ctx;
    private Timer timer;

    private WindowManager windowManager;
    private View mFloatingView;
    private LinearLayout patches;
    private TextView initText;
    private TextView expiry;
    private ESPView overlayView;

    public void Init(Context context, Service service, byte[] dex){
        try {
            ctx = context;
            floater = service;

            pkg = "com.tencent.ig";

            if (Shell.rootAccess()) {
                Shell.su("setenforce 0").submit();
            }

            String RAND = "RAND";

            if(!Prefs.with(context).contains(RAND)){
                Prefs.with(context).write(RAND, Utils.genDaemonName());
            }

            daemonPath = context.getCacheDir().getAbsolutePath() + "/" + Prefs.with(context).read(RAND);
            writeZipContentFile(dex, "libkdaemon.so", daemonPath);
            new File(daemonPath).setExecutable(true, true);
            System.loadLibrary("kmods");

            timer = new Timer();
            windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            overlayView = new ESPView(ctx);
            mFloatingView = LayoutInflater.from(ctx).inflate(getResID("activity_floating", "layout"), null);

            DrawCanvas();
            FloatButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Destroy() {
        Stop();
        isRunning = false;
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
            mFloatingView = null;
        }
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    private void DrawCanvas() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        windowManager.addView(overlayView, params);
    }

    private void FloatButton() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getLayoutType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        windowManager.addView(mFloatingView, params);
        final View floter = mFloatingView.findViewById(getID("floater_container"));
        final View menu = mFloatingView.findViewById(getID("menu_container"));

        patches = mFloatingView.findViewById(getID("patches"));

        floter.setVisibility(View.GONE);
        menu.setVisibility(View.VISIBLE);

        String htmltxt = "<html><head><style>body{color: white;font-weight:bold;font-family:Courier, monospace;}</style></head><body><marquee class=\"GeneratedMarquee\" direction=\"left\" scrollamount=\"4\" behavior=\"scroll\">PUBGPatcher By KMODs</marquee></body></html>";

        WebView wv = mFloatingView.findViewById(getID("webv"));
        wv.setBackgroundColor(Color.TRANSPARENT);
        wv.loadData(htmltxt, "text/html", "utf-8");

        mFloatingView.findViewById(getID("mclose")).setOnClickListener(view -> {
            floter.setVisibility(View.VISIBLE);
            menu.setVisibility(View.GONE);
        });
        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        int differenceX = (int) Math.abs(initialTouchX - event.getRawX());
                        int differenceY = (int) Math.abs(initialTouchY - event.getRawY());
                        if (differenceX < 10 && differenceY < 10 && floter.getVisibility() == View.VISIBLE) {
                            floter.setVisibility(View.GONE);
                            menu.setVisibility(View.VISIBLE);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        initText = addText("Waiting for Game to Run...");
        expiry = addText("Expiry Time: ");
        timer.scheduleAtFixedRate(new DRM(), 0, 60000);
    }

    class DRM extends TimerTask {
        private final String USER = "USER";
        private final String PASS = "PASS";
        private byte[] puk = {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -41, 36, -11, -27, -61, 32, 124, 58, 39, -94, -13, 7, 48, -104, -109, 106, -75, -8, -128, -92, -89, -125, -49, -83, 75, 12, -26, 90, 56, 35, 52, -116, 30, 40, -69, -70, 86, 14, -80, -20, 55, -89, 104, -46, -17, -80, -119, 83, -14, 116, -66, 11, -108, 5, 76, 12, -43, -89, -49, 11, 38, -124, 71, 45, 65, -103, 10, 99, 33, 79, 21, -16, -38, -60, 24, -108, 101, -89, -18, 48, -37, -78, 59, 10, 89, 42, 51, -43, 9, -33, -68, 61, -45, 94, -49, 83, 52, 56, 105, -123, 18, 89, 3, 54, -48, -63, -61, -103, -9, 79, -36, 18, 119, 11, -35, 82, 73, -66, 12, 123, -38, 97, 121, -30, 31, -50, -106, 127, 2, 3, 1, 0, 1};
        private byte[] crt = {45, 45, 45, 45, 45, 66, 69, 71, 73, 78, 32, 67, 69, 82, 84, 73, 70, 73, 67, 65, 84, 69, 45, 45, 45, 45, 45, 13, 10, 77, 73, 73, 70, 54, 122, 67, 67, 66, 78, 79, 103, 65, 119, 73, 66, 65, 103, 73, 82, 65, 73, 68, 53, 88, 53, 112, 118, 85, 72, 71, 112, 43, 81, 49, 84, 54, 88, 103, 117, 67, 78, 81, 119, 68, 81, 89, 74, 75, 111, 90, 73, 104, 118, 99, 78, 65, 81, 69, 76, 66, 81, 65, 119, 13, 10, 99, 106, 69, 76, 77, 65, 107, 71, 65, 49, 85, 69, 66, 104, 77, 67, 86, 86, 77, 120, 67, 122, 65, 74, 66, 103, 78, 86, 66, 65, 103, 84, 65, 108, 82, 89, 77, 82, 65, 119, 68, 103, 89, 68, 86, 81, 81, 72, 69, 119, 100, 73, 98, 51, 86, 122, 100, 71, 57, 117, 77, 82, 85, 119, 13, 10, 69, 119, 89, 68, 86, 81, 81, 75, 69, 119, 120, 106, 85, 71, 70, 117, 90, 87, 119, 115, 73, 69, 108, 117, 89, 121, 52, 120, 76, 84, 65, 114, 66, 103, 78, 86, 66, 65, 77, 84, 74, 71, 78, 81, 89, 87, 53, 108, 98, 67, 119, 103, 83, 87, 53, 106, 76, 105, 66, 68, 90, 88, 74, 48, 13, 10, 97, 87, 90, 112, 89, 50, 70, 48, 97, 87, 57, 117, 73, 69, 70, 49, 100, 71, 104, 118, 99, 109, 108, 48, 101, 84, 65, 101, 70, 119, 48, 120, 79, 84, 65, 51, 77, 68, 77, 119, 77, 68, 65, 119, 77, 68, 66, 97, 70, 119, 48, 120, 79, 84, 69, 119, 77, 68, 69, 121, 77, 122, 85, 53, 13, 10, 78, 84, 108, 97, 77, 66, 77, 120, 69, 84, 65, 80, 66, 103, 78, 86, 66, 65, 77, 84, 67, 71, 116, 116, 98, 50, 82, 122, 76, 109, 49, 115, 77, 73, 73, 66, 73, 106, 65, 78, 66, 103, 107, 113, 104, 107, 105, 71, 57, 119, 48, 66, 65, 81, 69, 70, 65, 65, 79, 67, 65, 81, 56, 65, 13, 10, 77, 73, 73, 66, 67, 103, 75, 67, 65, 81, 69, 65, 113, 68, 77, 48, 52, 103, 116, 121, 106, 80, 76, 114, 117, 97, 113, 103, 70, 84, 56, 71, 99, 49, 112, 103, 68, 48, 77, 49, 71, 78, 49, 86, 43, 49, 78, 56, 89, 102, 76, 50, 89, 65, 98, 48, 105, 100, 118, 48, 100, 87, 108, 116, 13, 10, 66, 106, 43, 116, 49, 100, 117, 82, 111, 80, 52, 55, 89, 43, 98, 81, 65, 69, 104, 79, 67, 43, 47, 110, 108, 97, 88, 113, 115, 115, 85, 65, 101, 116, 72, 68, 67, 69, 89, 100, 79, 54, 117, 110, 109, 88, 71, 113, 48, 82, 75, 119, 53, 114, 79, 80, 77, 103, 65, 113, 99, 81, 79, 48, 13, 10, 50, 86, 81, 85, 80, 86, 56, 120, 53, 119, 85, 111, 121, 85, 52, 76, 115, 121, 54, 98, 79, 57, 86, 48, 68, 83, 72, 120, 89, 77, 114, 71, 43, 97, 107, 83, 65, 56, 121, 73, 82, 87, 104, 77, 81, 84, 115, 51, 105, 109, 118, 78, 111, 73, 78, 83, 90, 111, 111, 70, 72, 110, 75, 100, 13, 10, 115, 83, 76, 68, 79, 89, 81, 78, 51, 118, 80, 112, 121, 53, 75, 121, 69, 116, 76, 110, 70, 84, 116, 88, 83, 89, 49, 74, 120, 79, 67, 71, 52, 52, 88, 114, 106, 119, 83, 82, 97, 122, 79, 66, 117, 84, 121, 102, 86, 98, 56, 100, 116, 88, 79, 83, 118, 78, 85, 101, 69, 88, 104, 52, 13, 10, 118, 107, 72, 54, 103, 109, 90, 98, 67, 83, 53, 98, 71, 99, 117, 88, 118, 89, 49, 71, 77, 114, 97, 68, 114, 117, 116, 50, 89, 83, 121, 72, 48, 71, 80, 102, 65, 73, 119, 75, 83, 112, 120, 90, 47, 52, 82, 74, 75, 97, 85, 67, 75, 49, 114, 56, 48, 103, 79, 57, 67, 121, 76, 50, 13, 10, 57, 78, 100, 117, 100, 101, 77, 100, 103, 71, 85, 72, 88, 122, 80, 80, 87, 70, 122, 56, 100, 73, 117, 72, 49, 119, 104, 109, 108, 56, 68, 48, 88, 119, 73, 68, 65, 81, 65, 66, 111, 52, 73, 67, 50, 84, 67, 67, 65, 116, 85, 119, 72, 119, 89, 68, 86, 82, 48, 106, 66, 66, 103, 119, 13, 10, 70, 111, 65, 85, 102, 103, 78, 97, 90, 85, 70, 114, 112, 51, 52, 75, 52, 98, 105, 100, 67, 79, 111, 100, 106, 104, 49, 113, 120, 50, 85, 119, 72, 81, 89, 68, 86, 82, 48, 79, 66, 66, 89, 69, 70, 77, 113, 103, 116, 109, 77, 102, 100, 97, 84, 87, 76, 108, 97, 107, 72, 107, 99, 116, 13, 10, 83, 112, 72, 77, 70, 122, 69, 48, 77, 65, 52, 71, 65, 49, 85, 100, 68, 119, 69, 66, 47, 119, 81, 69, 65, 119, 73, 70, 111, 68, 65, 77, 66, 103, 78, 86, 72, 82, 77, 66, 65, 102, 56, 69, 65, 106, 65, 65, 77, 66, 48, 71, 65, 49, 85, 100, 74, 81, 81, 87, 77, 66, 81, 71, 13, 10, 67, 67, 115, 71, 65, 81, 85, 70, 66, 119, 77, 66, 66, 103, 103, 114, 66, 103, 69, 70, 66, 81, 99, 68, 65, 106, 66, 80, 66, 103, 78, 86, 72, 83, 65, 69, 83, 68, 66, 71, 77, 68, 111, 71, 67, 121, 115, 71, 65, 81, 81, 66, 115, 106, 69, 66, 65, 103, 73, 48, 77, 67, 115, 119, 13, 10, 75, 81, 89, 73, 75, 119, 89, 66, 66, 81, 85, 72, 65, 103, 69, 87, 72, 87, 104, 48, 100, 72, 66, 122, 79, 105, 56, 118, 99, 50, 86, 106, 100, 88, 74, 108, 76, 109, 78, 118, 98, 87, 57, 107, 98, 121, 53, 106, 98, 50, 48, 118, 81, 49, 66, 84, 77, 65, 103, 71, 66, 109, 101, 66, 13, 10, 68, 65, 69, 67, 65, 84, 66, 77, 66, 103, 78, 86, 72, 82, 56, 69, 82, 84, 66, 68, 77, 69, 71, 103, 80, 54, 65, 57, 104, 106, 116, 111, 100, 72, 82, 119, 79, 105, 56, 118, 89, 51, 74, 115, 76, 109, 78, 118, 98, 87, 57, 107, 98, 50, 78, 104, 76, 109, 78, 118, 98, 83, 57, 106, 13, 10, 85, 71, 70, 117, 90, 87, 120, 74, 98, 109, 78, 68, 90, 88, 74, 48, 97, 87, 90, 112, 89, 50, 70, 48, 97, 87, 57, 117, 81, 88, 86, 48, 97, 71, 57, 121, 97, 88, 82, 53, 76, 109, 78, 121, 98, 68, 66, 57, 66, 103, 103, 114, 66, 103, 69, 70, 66, 81, 99, 66, 65, 81, 82, 120, 13, 10, 77, 71, 56, 119, 82, 119, 89, 73, 75, 119, 89, 66, 66, 81, 85, 72, 77, 65, 75, 71, 79, 50, 104, 48, 100, 72, 65, 54, 76, 121, 57, 106, 99, 110, 81, 117, 89, 50, 57, 116, 98, 50, 82, 118, 89, 50, 69, 117, 89, 50, 57, 116, 76, 50, 78, 81, 89, 87, 53, 108, 98, 69, 108, 117, 13, 10, 89, 48, 78, 108, 99, 110, 82, 112, 90, 109, 108, 106, 89, 88, 82, 112, 98, 50, 53, 66, 100, 88, 82, 111, 98, 51, 74, 112, 100, 72, 107, 117, 89, 51, 74, 48, 77, 67, 81, 71, 67, 67, 115, 71, 65, 81, 85, 70, 66, 122, 65, 66, 104, 104, 104, 111, 100, 72, 82, 119, 79, 105, 56, 118, 13, 10, 98, 50, 78, 122, 99, 67, 53, 106, 98, 50, 49, 118, 90, 71, 57, 106, 89, 83, 53, 106, 98, 50, 48, 119, 77, 65, 89, 68, 86, 82, 48, 82, 66, 67, 107, 119, 74, 52, 73, 73, 97, 50, 49, 118, 90, 72, 77, 117, 98, 87, 121, 67, 68, 87, 49, 104, 97, 87, 119, 117, 97, 50, 49, 118, 13, 10, 90, 72, 77, 117, 98, 87, 121, 67, 68, 72, 100, 51, 100, 121, 53, 114, 98, 87, 57, 107, 99, 121, 53, 116, 98, 68, 67, 67, 65, 81, 81, 71, 67, 105, 115, 71, 65, 81, 81, 66, 49, 110, 107, 67, 66, 65, 73, 69, 103, 102, 85, 69, 103, 102, 73, 65, 56, 65, 66, 50, 65, 76, 118, 90, 13, 10, 51, 55, 119, 102, 105, 110, 71, 49, 107, 53, 81, 106, 108, 54, 113, 83, 101, 48, 99, 52, 86, 53, 85, 75, 113, 49, 76, 111, 71, 112, 67, 87, 90, 68, 97, 79, 72, 116, 71, 70, 65, 65, 65, 66, 97, 55, 108, 90, 120, 106, 81, 65, 65, 65, 81, 68, 65, 69, 99, 119, 82, 81, 73, 104, 13, 10, 65, 79, 87, 50, 80, 65, 114, 105, 114, 77, 84, 54, 57, 116, 75, 52, 112, 107, 121, 50, 108, 108, 74, 84, 51, 112, 117, 79, 77, 122, 76, 114, 106, 73, 87, 82, 52, 119, 71, 54, 119, 72, 104, 49, 65, 105, 65, 116, 117, 107, 121, 114, 78, 111, 71, 86, 90, 65, 87, 115, 43, 50, 74, 101, 13, 10, 65, 107, 57, 107, 87, 76, 104, 57, 76, 72, 121, 104, 98, 66, 80, 43, 49, 66, 83, 68, 121, 50, 74, 71, 100, 119, 66, 50, 65, 72, 82, 43, 50, 111, 77, 120, 114, 84, 77, 81, 107, 83, 71, 99, 122, 105, 86, 80, 81, 110, 68, 67, 118, 47, 49, 101, 81, 105, 65, 73, 120, 106, 99, 49, 13, 10, 101, 101, 89, 81, 101, 56, 120, 87, 65, 65, 65, 66, 97, 55, 108, 90, 120, 108, 77, 65, 65, 65, 81, 68, 65, 69, 99, 119, 82, 81, 73, 103, 81, 102, 103, 118, 90, 66, 85, 80, 100, 102, 122, 119, 100, 67, 83, 67, 101, 118, 77, 80, 75, 52, 79, 57, 105, 80, 111, 101, 101, 75, 98, 74, 13, 10, 67, 120, 56, 85, 89, 69, 109, 74, 105, 109, 111, 67, 73, 81, 68, 121, 120, 107, 43, 113, 98, 104, 85, 70, 54, 49, 89, 53, 114, 79, 68, 117, 47, 52, 122, 72, 118, 116, 116, 105, 53, 66, 56, 85, 114, 120, 65, 73, 109, 82, 109, 86, 52, 115, 65, 85, 117, 68, 65, 78, 66, 103, 107, 113, 13, 10, 104, 107, 105, 71, 57, 119, 48, 66, 65, 81, 115, 70, 65, 65, 79, 67, 65, 81, 69, 65, 70, 72, 116, 51, 47, 102, 87, 115, 80, 111, 110, 47, 55, 74, 81, 72, 84, 50, 66, 76, 82, 111, 50, 112, 55, 103, 111, 73, 73, 51, 52, 112, 115, 121, 103, 67, 50, 55, 72, 106, 87, 120, 110, 109, 13, 10, 57, 50, 121, 102, 107, 74, 68, 106, 113, 87, 99, 90, 56, 110, 79, 48, 113, 73, 114, 116, 100, 79, 85, 84, 103, 114, 115, 108, 82, 83, 89, 67, 111, 71, 97, 108, 111, 49, 119, 90, 81, 54, 107, 73, 71, 89, 117, 71, 115, 97, 89, 121, 110, 71, 78, 68, 67, 56, 109, 47, 86, 76, 52, 75, 13, 10, 116, 72, 65, 110, 120, 108, 49, 79, 106, 49, 105, 68, 77, 117, 43, 48, 89, 115, 113, 114, 69, 80, 72, 107, 78, 111, 81, 109, 118, 99, 108, 47, 75, 86, 88, 74, 47, 121, 74, 122, 68, 56, 118, 114, 69, 74, 47, 107, 110, 103, 55, 106, 51, 85, 51, 101, 115, 122, 98, 114, 69, 71, 117, 90, 13, 10, 79, 54, 77, 122, 105, 90, 47, 54, 121, 105, 56, 56, 108, 65, 109, 65, 81, 71, 79, 103, 105, 84, 108, 76, 117, 113, 69, 68, 97, 110, 57, 112, 107, 116, 76, 72, 78, 55, 84, 115, 82, 53, 85, 50, 119, 84, 103, 68, 112, 50, 111, 83, 114, 116, 50, 52, 110, 49, 111, 90, 116, 47, 115, 117, 13, 10, 49, 53, 66, 47, 82, 119, 55, 97, 116, 74, 78, 43, 49, 66, 110, 83, 43, 47, 102, 81, 108, 51, 98, 118, 57, 80, 97, 108, 56, 48, 65, 80, 66, 88, 107, 121, 100, 97, 90, 109, 50, 90, 120, 86, 68, 103, 49, 65, 83, 105, 47, 118, 119, 120, 55, 68, 49, 106, 104, 101, 71, 81, 111, 100, 13, 10, 56, 66, 115, 88, 48, 53, 110, 80, 73, 83, 51, 68, 111, 97, 79, 83, 121, 85, 76, 83, 106, 66, 76, 120, 109, 111, 51, 111, 86, 81, 106, 69, 55, 116, 48, 106, 99, 115, 66, 47, 87, 65, 61, 61, 13, 10, 45, 45, 45, 45, 45, 69, 78, 68, 32, 67, 69, 82, 84, 73, 70, 73, 67, 65, 84, 69, 45, 45, 45, 45, 45};

        private void TOAST(String what) throws InterruptedException {
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(ctx, what, Toast.LENGTH_LONG).show();
            });
            Thread.sleep(1200);
            new Handler(Looper.getMainLooper()).post(() -> {
                floater.stopSelf();
            });
        }

        @Override
        public void run() {
            try {
                if(Thread.currentThread().isInterrupted()){
                    return;
                }

                JSONObject token = new JSONObject();

                JSONObject data = new JSONObject();
                data.put("uname", Prefs.with(floater).read(USER));
                data.put("pass", Prefs.with(floater).read(PASS));
                data.put("load", "0");
                data.put("cs", getUniqueId(ctx));

                token.put("Data", RSA.encrypt(data.toString(),puk));
                token.put("Hash", Utils.SHA256(data.toString()));

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca = cf.generateCertificate(new ByteArrayInputStream(crt));

                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

                HttpsURLConnection urlConnection = (HttpsURLConnection) new URL("https://kmods.ml/Login").openConnection();
                urlConnection.setSSLSocketFactory(context.getSocketFactory());
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postParameters = "token=" + Utils.toBase64(token.toString());
                urlConnection.setFixedLengthStreamingMode(postParameters.getBytes().length);
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(postParameters);
                out.close();

                String resp = Utils.readStream(urlConnection.getInputStream());
                if(!resp.isEmpty()) {
                    JSONObject ack = new JSONObject(Utils.fromBase64String(resp));
                    String decData = Utils.profileDecrypt(ack.get("Data").toString(), ack.get("Hash").toString());
                    if (!RSA.verify(decData, ack.get("Sign").toString(), puk)) {
                        TOAST("Login Data is Wrong!");
                    }
                    data = new JSONObject(decData);
                    if(data.get("Status").toString().equals("Success")) {
                        String exp = "Expiry Time: " + timeConvert(Integer.parseInt(data.get("SubscriptionLeft").toString()));
                        new Handler(Looper.getMainLooper()).post(() -> {
                            expiry.setText(exp);
                        });
                        if (!isRunning) {
                            startDaemon();
                            Thread.sleep(1200);
                            if (Init() < 0) {
                                TOAST("Game Not Running!");
                            } else {
                                isRunning = true;
                                new Handler(Looper.getMainLooper()).post(Loader.this::startServer);
                            }
                        }
                    } else {
                        TOAST(data.get("MessageString").toString());
                    }
                    urlConnection.disconnect();
                } else {
                    TOAST("Error From Server!");
                }
            } catch (Exception e){
                try {
                    e.printStackTrace();
                    Log.e("LoaderError", e.getMessage());
                    TOAST("Error in Background");
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void startServer(){
        patches.removeView(initText);
        addSpace(16);
        addText("-:ESP Hacks:-");
        addSwitch("Enable ESP", (buttonView, isChecked) -> {
            Switch(0, isChecked);
        });
        addSpace(16);
        addText("-:ESP Player:-");
        addSwitch("Name", (buttonView, isChecked) -> Switch(1, isChecked));
        addSwitch("Health", (buttonView, isChecked) -> Switch(2, isChecked));
        addSwitch("Distance", (buttonView, isChecked) -> Switch(3, isChecked));
        addSwitch("TeamMate", (buttonView, isChecked) -> Switch(4, isChecked));
        addSeekbar(30, new SeekBar.OnSeekBarChangeListener() {
            TextView sizetext = addText("PlayerESP Size: 4");
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int pindex = (progress + 4);
                float psize = (progress + 4.0f);
                String tsize = "PlayerESP Size: " + pindex;
                sizetext.setText(tsize);
                Size(0, psize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        addSpace(16);
        addText("-:ESP Types:-");
        addSwitch("LineESP", (buttonView, isChecked) -> Switch(5, isChecked));
        addSwitch("BoxESP", (buttonView, isChecked) -> Switch(6, isChecked));
        addSwitch("360Alert", (buttonView, isChecked) -> Switch(7, isChecked));
        addSpace(16);
        addText("-:ESP Vehicles:-");
        addSwitch("Name", (buttonView, isChecked) -> Switch(8, isChecked));
        addSwitch("Distance", (buttonView, isChecked) -> Switch(9, isChecked));
        addSeekbar(30, new SeekBar.OnSeekBarChangeListener() {
            TextView sizetext = addText("VehicleESP Size: 4");
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int pindex = (progress + 4);
                float psize = (progress + 4.0f);
                String tsize = "VehiclerESP Size: " + pindex;
                sizetext.setText(tsize);
                Size(1, psize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        addSpace(16);
        addText("-:ESP LootBox and AirDrop:-");
        addSwitch("Name", (buttonView, isChecked) -> Switch(10, isChecked));
        addSwitch("Distance", (buttonView, isChecked) -> Switch(11, isChecked));
        addSeekbar(30, new SeekBar.OnSeekBarChangeListener() {
            TextView sizetext = addText("LootESP Size: 4");
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int pindex = (progress + 4);
                float psize = (progress + 4.0f);
                String tsize = "LootESP Size: " + pindex;
                sizetext.setText(tsize);
                Size(1, psize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startDaemon(){
        new Thread(() -> {
            String cmd = daemonPath + " " + getProcessID();
            if (Shell.rootAccess()) {
                Shell.su(cmd).submit();
            } else {
                Shell.sh(cmd).submit();
            }
        }).start();
    }

    private int getProcessID() {
        int pid = -1;
        if (Shell.rootAccess()) {
            String cmd = "for p in /proc/[0-9]*; do [[ $(<$p/cmdline) = " + pkg + " ]] && echo ${p##*/}; done";
            List<String> outs = new ArrayList<>();
            Shell.su(cmd).to(outs).exec();
            if (outs.size() > 0) {
                pid = Integer.parseInt(outs.get(0));
            }
        } else {
            Shell.Result out = Shell.sh("/system/bin/ps -A | grep \"" + pkg + "\"").exec();
            List<String> output = out.getOut();
            if (output.isEmpty() || output.get(0).contains("bad pid")) {
                out = Shell.sh("/system/bin/ps | grep \"" + pkg + "\"").exec();
                output = out.getOut();
                if (!output.isEmpty() && !output.get(0).contains("bad pid")) {
                    for (int i = 0; i < output.size(); i++) {
                        String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                        if (results[8].equals(pkg)) {
                            pid = Integer.parseInt(results[1]);
                        }
                    }
                }
            } else {
                for (int i = 0; i < output.size(); i++) {
                    String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                    for (int j = 0; j < results.length; j++) {
                        if (results[j].equals(pkg)) {
                            pid = Integer.parseInt(results[j - 7]);
                        }
                    }
                }
            }
        }
        return pid;
    }

    //Native Funcs
    public static native int Init();

    public static native void DrawOn(ESPView espView, Canvas canvas);

    public static native void Switch(int num, boolean flag);

    public static native void Size(int num, float size);

    public static native void Stop();

    //UI Elements
    private void addSpace(int space) {
        View separator = new View(ctx);
        LinearLayout.LayoutParams params = setParams();
        params.height = space;
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.TRANSPARENT);
        patches.addView(separator);
    }

    private void addSwitch(String name, CompoundButton.OnCheckedChangeListener listener) {
        final Switch sw = new Switch(ctx);
        sw.setText(name);
        sw.setTextSize(dipToPixels());
        sw.setTextColor(Color.WHITE);
        sw.getThumbDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        sw.setOnClickListener(view -> {
            if (sw.isChecked()) {
                sw.getThumbDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
            } else {
                sw.getThumbDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            }
        });
        sw.setOnCheckedChangeListener(listener);
        sw.setLayoutParams(setParams());
        patches.addView(sw);
        addSpace(12);
    }

    private void addSeekbar(int max, final SeekBar.OnSeekBarChangeListener listener) {
        SeekBar sb = new SeekBar(ctx);
        sb.setMax(max);
        sb.setLayoutParams(setParams());
        sb.setOnSeekBarChangeListener(listener);
        patches.addView(sb);
        addSpace(12);
    }

    private TextView addText(String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(getBestTextSize());
        tv.setTextColor(Color.WHITE);
        tv.setLayoutParams(setParams());
        patches.addView(tv);
        addSpace(12);
        return tv;
    }

    private LinearLayout.LayoutParams setParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        return params;
    }

    private boolean isTablet() {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        return diagonalInches >= 6.5;
    }

    private float getBestTextSize() {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        float d = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, metrics);
        if (isTablet())
            d += 7.f;
        return (d > 20 && !isTablet()) ? 20 : d;
    }

    private float dipToPixels() {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, metrics);
    }

    private int getLayoutType() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        return LAYOUT_FLAG;
    }

    private int getResID(String name, String type) {
        return ctx.getResources().getIdentifier(name, type, ctx.getPackageName());
    }

    private int getID(String name) {
        return getResID(name, "id");
    }

    private static void writeZipContentFile(byte[] inFile, String infname, String outFile) throws IOException {
        File fileOut = new File(outFile);
        byte[] buffer = new byte[2048];
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(inFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!(entry.getName().contains(infname) && entry.getName().contains(currArch()))) continue;
                try (FileOutputStream output = new FileOutputStream(fileOut)) {
                    int len;
                    while ((len = zipIn.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                }
                break;
            }
        }
    }

    private static String currArch(){
        String arch = System.getProperty("os.arch");
        if(arch != null){
            if((arch.contains("armv8l") || arch.contains("aarch64")))
                return "arm64-v8a";
            if((arch.contains("i686")))
                return "x86";
        }
        return "armeabi-v7a";
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

    private String getUniqueId(Context ctx) {
        String key = (getDeviceName() + Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID) + Build.HARDWARE).replace(" ", "");
        UUID uniqueKey = UUID.nameUUIDFromBytes(key.getBytes());
        return uniqueKey.toString().replace("-", "");
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }
}
