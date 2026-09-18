package com.rfaulkne.karoofueltimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FuelActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        FuelStore.log(context, intent.action ?: return)
    }
}
