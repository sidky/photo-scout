package com.github.sidky.photoscout

import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.github.sidky.photoscout.PhotoScoutApplication

class DebugPhotoScoutApplication : PhotoScoutApplication() {
    override fun onCreate() {
        super.onCreate()

        SoLoader.init(this, false)

        val client = AndroidFlipperClient.getInstance(this)
        client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
        client.start()
    }
}