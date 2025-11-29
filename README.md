# FridaLoader (Self-Contained)

This project builds an Xposed module to load Frida Gadget.
It includes its own Xposed API stubs, so it builds offline without JitPack errors.

## Critical Step
You must manually add the Frida Gadget library:
1. Download `frida-gadget-android-arm64.so`
2. Rename to `libfrida-gadget.so`
3. Place in: `app/src/main/jniLibs/arm64-v8a/`