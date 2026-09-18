package com.rfaulkne.karoofueltimer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.max

class FuelTimerDataType(extension: String) : DataTypeImpl(extension, TYPE_ID) {

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            while (isActive) {
                val snapshot = FuelStore.snapshot(context)
                emitter.updateView(buildView(context, snapshot))

                withTimeoutOrNull(30_000L) {
                    FuelStore.events.first()
                }
            }
        }

        emitter.setCancellable { scope.cancel() }
    }

    private fun buildView(context: Context, snapshot: FuelSnapshot): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.fuel_timer_field)

        views.setTextViewText(R.id.bar_time, elapsed(snapshot.lastBarMs))
        views.setTextViewText(R.id.gel_time, elapsed(snapshot.lastGelMs))
        views.setTextViewText(R.id.bar_count, snapshot.barCount.toString())
        views.setTextViewText(R.id.gel_count, snapshot.gelCount.toString())

        views.setOnClickPendingIntent(R.id.btn_bar, pending(context, 101, FuelStore.ACTION_BAR))
        views.setOnClickPendingIntent(R.id.btn_gel, pending(context, 102, FuelStore.ACTION_GEL))

        return views
    }

    private fun pending(context: Context, requestCode: Int, action: String): PendingIntent {
        val intent = Intent(context, FuelActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun elapsed(timestampMs: Long): String {
        if (timestampMs <= 0L) return "—"
        val minutes = max(0L, (System.currentTimeMillis() - timestampMs) / 60_000L)
        return "$minutes min"
    }

    companion object {
        const val TYPE_ID = "fuel-grid-v2"
    }
}
