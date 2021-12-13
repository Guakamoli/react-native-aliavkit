package com.rncamerakit

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.rncamerakit.editor.CKEditorManager
import com.rncamerakit.editor.RNEditorKitModule
import com.rncamerakit.recorder.CKCameraManager
import com.rncamerakit.recorder.RNCameraKitModule
import java.util.*

class RNCameraKitPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules: MutableList<NativeModule> = ArrayList()
        modules.add(RNCameraKitModule(reactContext))
        modules.add(RNEditorKitModule(reactContext))
        return modules
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        val viewManagers: MutableList<ViewManager<*, *>> = ArrayList()
        viewManagers.add(CKCameraManager())
        viewManagers.add(CKEditorManager())
        return viewManagers
    }
}