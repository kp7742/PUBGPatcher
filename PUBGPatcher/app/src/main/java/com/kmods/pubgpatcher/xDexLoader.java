package com.kmods.pubgpatcher;

import android.app.Service;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import dalvik.system.BaseDexClassLoader;

class xDexLoader {
    private static Object ob;
    private static Method initmethod;
    private static Method desmethod;
    private static byte[] dex;
    private static String DexPath;
    private static Context ctx;

    static void Setup(Context contx, byte[] dexdata) {
        ctx = contx;
        dex = Utils.loaderDecrypt(dexdata);
        DexPath = ctx.getCacheDir() + "/helpshiftv3.jar";
        TrySetupLoader();
    }

    static void Init(Context context, Service service) {
        try {
            initmethod.invoke(ob, context, service, dex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void Destroy() {
        try {
            desmethod.invoke(ob);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void LoadBaseDex() {
        try {
            BaseDexClassLoader dexLoader = new BaseDexClassLoader(DexPath, ctx.getCacheDir(), ctx.getApplicationInfo().nativeLibraryDir, ctx.getClassLoader());
            Class<?> tmpClass = dexLoader.loadClass("com.kmods.loader.Loader");
            ob = tmpClass.newInstance();
            initmethod = tmpClass.getMethod("Init", Context.class, Service.class, byte[].class);
            initmethod.setAccessible(true);
            desmethod = tmpClass.getMethod("Destroy");
            desmethod.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void TrySetupLoader() {
        try {
            FileOutputStream fo = new FileOutputStream(DexPath);
            fo.write(dex);
            fo.flush();
            fo.close();
            LoadBaseDex();
            rmFile(DexPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void rmFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}