package com.example.fridaloader;

import android.app.Application;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.snapchat.android";
    private static final String LIB_NAME = "libfrida-gadget.so";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedBridge.log("FridaLoader: Target detected. Waiting for Application.onCreate...");

        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application app = (Application) param.thisObject;
                loadFrida(app);
            }
        });
    }

    private void loadFrida(Context context) {
        try {
            // 1. Try standard loading first
            try {
                System.loadLibrary("frida-gadget");
                XposedBridge.log("FridaLoader: Standard System.loadLibrary success!");
                return;
            } catch (Throwable t) {
                XposedBridge.log("FridaLoader: Standard load failed. Attempting manual extraction...");
            }

            // 2. Manual Extraction from Module Context
            // LSPatch merges resources, so we might find it in assets or nativeLibraryDir of the module
            // But in Integrated mode, resources are merged. 
            // NOTE: Accessing the module's OWN resources in integrated mode is tricky.
            
            // PLAN B: We assume the lib is in the APK's lib path but not registered.
            // Let's try to load it from the application's nativeLibraryDir directly by absolute path.
            
            String nativeDir = context.getApplicationInfo().nativeLibraryDir;
            File libFile = new File(nativeDir, LIB_NAME);
            if (libFile.exists()) {
                 System.load(libFile.getAbsolutePath());
                 XposedBridge.log("FridaLoader: Loaded from absolute path: " + libFile.getAbsolutePath());
                 return;
            }

            // PLAN C: Extract from ClassLoader resources (if it was bundled as a Java resource)
            // This requires you to put the .so file in 'src/main/resources/lib/arm64-v8a/' instead of jniLibs
            // For now, let's stick to the most likely fix:
            
            XposedBridge.log("FridaLoader: FATAL - Could not find library file in: " + nativeDir);
            
        } catch (Throwable t) {
            XposedBridge.log("FridaLoader: Final Load Failure: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
