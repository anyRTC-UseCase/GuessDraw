package io.anyrtc.teamview

import android.app.Application
import io.anyrtc.drawsomething.utils.SpUtil
import kotlin.properties.Delegates

class App : Application() {

    companion object{
        var app : App by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        SpUtil.init(this)
    }
}
