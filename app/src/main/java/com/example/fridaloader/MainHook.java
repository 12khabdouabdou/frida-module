package com.example.fridaloader;

import android.app.Application;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // CHANGE THIS to the package name you want to target (e.g., com.snapchat.android)
    // or leave it null to attempt loading in ALL apps (not recommended)
    private static final String TARGET_PACKAGE = "com.snapchat.android";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Filter for our target package
        if (TARGET_PACKAGE != null && !lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("FridaLoader: Targeted package found: " + lpparam.packageName);

        // We hook Application.onCreate to ensure the app context is initialized 
        // and native libraries are ready to be loaded.
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    // Attempt to load the library. 
                    // When using LSPatch/LSPosed, libraries in 'jniLibs' are added to the 
                    // app's native library path automatically.
                    System.loadLibrary("frida-gadget");
                    XposedBridge.log("FridaLoader: SUCCESS - System.loadLibrary('frida-gadget') executed.");
                } catch (UnsatisfiedLinkError e) {
                    XposedBridge.log("FridaLoader: ERROR - Could not load library. Ensure libfrida-gadget.so is in jniLibs/arm64-v8a/");
                    XposedBridge.log(e.getMessage());
                } catch (Throwable t) {
                    XposedBridge.log("FridaLoader: ERROR - Generic exception during load.");
                    XposedBridge.log(t.getMessage());
                }
            }
        });
    }
}