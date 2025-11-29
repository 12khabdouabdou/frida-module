package com.example.fridaloader;

import android.app.Application;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // Target package (Snapchat)
    private static final String TARGET_PACKAGE = "com.snapchat.android";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedBridge.log("FridaLoader: Injecting into " + lpparam.packageName);

        // Hook Application.onCreate to load the library
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    // Loads libfrida-gadget.so from the app's native library path
                    System.loadLibrary("frida-gadget");
                    XposedBridge.log("FridaLoader: System.loadLibrary('frida-gadget') success!");
                } catch (Throwable t) {
                    XposedBridge.log("FridaLoader: Failed to load library: " + t.getMessage());
                }
            }
        });
    }
}