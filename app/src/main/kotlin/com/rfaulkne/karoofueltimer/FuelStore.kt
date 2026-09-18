package com.rfaulkne.karoofueltimer

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow

data class FuelSnapshot(
    val lastBarMs: Long,
    val barSize: String,
    val lastGelMs: Long,
    val gelSize: String,
    val lastGlucoseMs: Long,
    val total: Int,
)

data class FuelEvent(val code: Int, val total: Int, val timestampMs: Long)

object FuelStore {
    const val ACTION_BAR_S = "com.rfaulkne.karoofueltimer.BAR_S"
    const val ACTION_BAR_L = "com.rfaulkne.karoofueltimer.BAR_L"
    const val ACTION_GEL_S = "com.rfaulkne.karoofueltimer.GEL_S"
    const val ACTION_GEL_L = "com.rfaulkne.karoofueltimer.GEL_L"
    const val ACTION_GLUCOSE = "com.rfaulkne.karoofueltimer.GLUCOSE"

    val events = MutableSharedFlow<FuelEvent>(extraBufferCapacity = 32)

    private const val PREFS = "fuel_timer"
    private const val LAST_BAR = "last_bar"
    private const val BAR_SIZE = "bar_size"
    private const val LAST_GEL = "last_gel"
    private const val GEL_SIZE = "gel_size"
    private const val LAST_GLUCOSE = "last_glucose"
    private const val TOTAL = "total"

    fun snapshot(context: Context): FuelSnapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return FuelSnapshot(
            lastBarMs = p.getLong(LAST_BAR, 0L),
            barSize = p.getString(BAR_SIZE, "") ?: "",
            lastGelMs = p.getLong(LAST_GEL, 0L),
            gelSize = p.getString(GEL_SIZE, "") ?: "",
            lastGlucoseMs = p.getLong(LAST_GLUCOSE, 0L),
            total = p.getInt(TOTAL, 0),
        )
    }

    fun log(context: Context, action: String) {
        val now = System.currentTimeMillis()
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val nextTotal = p.getInt(TOTAL, 0) + 1

        val code = when (action) {
            ACTION_BAR_S -> {
                p.edit().putLong(LAST_BAR, now).putString(BAR_SIZE, "S").putInt(TOTAL, nextTotal).apply()
                1
            }
            ACTION_BAR_L -> {
                p.edit().putLong(LAST_BAR, now).putString(BAR_SIZE, "L").putInt(TOTAL, nextTotal).apply()
                2
            }
            ACTION_GEL_S -> {
                p.edit().putLong(LAST_GEL, now).putString(GEL_SIZE, "S").putInt(TOTAL, nextTotal).apply()
                3
            }
            ACTION_GEL_L -> {
                p.edit().putLong(LAST_GEL, now).putString(GEL_SIZE, "L").putInt(TOTAL, nextTotal).apply()
                4
            }
            ACTION_GLUCOSE -> {
                p.edit().putLong(LAST_GLUCOSE, now).putInt(TOTAL, nextTotal).apply()
                5
            }
            else -> return
        }

        events.tryEmit(FuelEvent(code, nextTotal, now))
    }

    fun resetRide(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
