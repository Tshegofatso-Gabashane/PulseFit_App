package com.example.pulsefit_app

import android.app.Application

class PulseFitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PushHelper.createChannel(this)
    }
}