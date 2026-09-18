package com.rfaulkne.karoofueltimer

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow

data class FuelSnapshot(
    val lastBarMs: Long,
    val barCount: Int,
    val lastGelMs: Long,
    val gelCount: Int,
    val total: Int,
)

data class FuelEvent(val code: Int, val total: Int, val timestampMs: Long)

object FuelStore {
    const val ACTION_BAR = "com.rfaulkne.karoofueltimer.BAR"
    const val ACTION_GEL = "com.rfaulkne.karoofueltimer.GEL"

    val events = MutableSharedFlow<FuelEvent>(extraBufferCapacity = 32)

    private const val PREFS = "fuel_timer"
    private const val LAST_BAR = "last_bar"
    private const val BAR_COUNT = "bar_count"
    private const val LAST_GEL = "last_gel"
    private const val GEL_COUNT = "gel_count"
    private const val TOTAL = "total"
    private const val SESSION_ACTIVE = "session_active"

    fun snapshot(context: Context): FuelSnapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return FuelSnapshot(
            lastBarMs = p.getLong(LAST_BAR, 0L),
            barCount = p.getInt(BAR_COUNT, 0),
            lastGelMs = p.getLong(LAST_GEL, 0L),
            gelCount = p.getInt(GEL_COUNT, 0),
            total = p.getInt(TOTAL, 0),
        )
    }

    fun log(context: Context, action: String) {
        val now = System.currentTimeMillis()
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nextTotal = p.getInt(TOTAL, 0) + 1

        val code = when (action) {
            ACTION_BAR -> {
                p.edit()
                    .putLong(LAST_BAR, now)
                    .putInt(BAR_COUNT, p.getInt(BAR_COUNT, 0) + 1)
                    .putInt(TOTAL, nextTotal)
                    .apply()
                1
            }
            ACTION_GEL -> {
                p.edit()
                    .putLong(LAST_GEL, now)
                    .putInt(GEL_COUNT, p.getInt(GEL_COUNT, 0) + 1)
                    .putInt(TOTAL, nextTotal)
                    .apply()
                2
            }
            else -> return
        }

        events.tryEmit(FuelEvent(code, nextTotal, now))
    }

    fun onRideIdle(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SESSION_ACTIVE, false)
            .apply()
    }

    fun onRideActive(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.getBoolean(SESSION_ACTIVE, false)) {
            p.edit().clear().putBoolean(SESSION_ACTIVE, true).apply()
        }
    }
}
