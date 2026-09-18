package com.rfaulkne.karoofueltimer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class FuelTimerDataType(extension: String) : DataTypeImpl(extension, TYPE_ID) {

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        val job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                emitter.updateView(buildView(context))
                delay(30_000)
            }
        }

        emitter.setCancellable { job.cancel() }
    }

    private fun buildView(context: Context): RemoteViews {
        val snapshot = FuelStore.snapshot(context)
        val views = RemoteViews(context.packageName, R.layout.fuel_timer_field)

        views.setTextViewText(R.id.bar_label, categoryLabel(if (snapshot.barSize.isBlank()) "BAR" else "BAR " + snapshot.barSize, snapshot.barCount))
        views.setTextViewText(R.id.gel_label, categoryLabel(if (snapshot.gelSize.isBlank()) "GEL" else "GEL " + snapshot.gelSize, snapshot.gelCount))
        views.setTextViewText(R.id.glu_label, categoryLabel("GLU", snapshot.glucoseCount))
        views.setTextViewText(R.id.bar_time, elapsed(snapshot.lastBarMs))
        views.setTextViewText(R.id.gel_time, elapsed(snapshot.lastGelMs))
        views.setTextViewText(R.id.glu_time, elapsed(snapshot.lastGlucoseMs))

        views.setOnClickPendingIntent(R.id.btn_bar_s, pending(context, 101, FuelStore.ACTION_BAR_S))
        views.setOnClickPendingIntent(R.id.btn_bar_l, pending(context, 102, FuelStore.ACTION_BAR_L))
        views.setOnClickPendingIntent(R.id.btn_gel_s, pending(context, 103, FuelStore.ACTION_GEL_S))
        views.setOnClickPendingIntent(R.id.btn_gel_l, pending(context, 104, FuelStore.ACTION_GEL_L))
        views.setOnClickPendingIntent(R.id.btn_glucose, pending(context, 105, FuelStore.ACTION_GLUCOSE))

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

    private fun categoryLabel(name: String, count: Int): CharSequence {
        val text = "$name\n$count total"
        val result = SpannableString(text)
        val start = name.length + 1
        result.setSpan(RelativeSizeSpan(0.58f), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        result.setSpan(ForegroundColorSpan(0xFFBDBDBD.toInt()), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return result
    }\n\n    companion object {
        const val TYPE_ID = "fuel-timer"
    }
}
