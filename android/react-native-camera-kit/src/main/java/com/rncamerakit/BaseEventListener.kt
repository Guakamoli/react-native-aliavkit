package com.rncamerakit

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WindowFocusChangeListener

class BaseEventListener : ActivityEventListener, LifecycleEventListener,
    WindowFocusChangeListener {

    private var mEventListener: LifecycleEventListener? = null

    open abstract class LifecycleEventListener {
        open fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {}
        open fun onNewIntent(intent: Intent?) {}
        open fun onHostResume() {}
        open fun onHostPause() {}
        open fun onHostDestroy() {}
        open fun onWindowFocusChange(hasFocus: Boolean) {}
    }

    constructor(reactContext: ReactContext) {
        reactContext.addWindowFocusChangeListener(this)
        reactContext.addLifecycleEventListener(this)
        reactContext.addWindowFocusChangeListener(this)
    }

    constructor(reactContext: ReactContext, listener: LifecycleEventListener?) {
        reactContext.addWindowFocusChangeListener(this)
        reactContext.addLifecycleEventListener(this)
        reactContext.addWindowFocusChangeListener(this)
        mEventListener = listener
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent) {
        mEventListener?.onActivityResult(activity, requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        mEventListener?.onNewIntent(intent)
    }

    override fun onHostResume() {
        mEventListener?.onHostResume()
    }

    override fun onHostPause() {
        mEventListener?.onHostPause()
    }

    override fun onHostDestroy() {
        mEventListener?.onHostDestroy()
    }

    override fun onWindowFocusChange(hasFocus: Boolean) {
        mEventListener?.onWindowFocusChange(hasFocus)
    }

}
