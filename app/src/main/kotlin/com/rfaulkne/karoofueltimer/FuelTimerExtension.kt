package com.rfaulkne.karoofueltimer

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DeveloperField
import io.hammerhead.karooext.models.FieldValue
import io.hammerhead.karooext.models.FitEffect
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.WriteEventMesg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FuelTimerExtension : KarooExtension("fuel-timer", "0.5") {
    private val karooSystem by lazy { KarooSystemService(this) }
    private var serviceJob: Job? = null

    override val types by lazy {
        listOf(FuelTimerDataType(extension))
    }

    private val fuelCodeField = DeveloperField(
        fieldDefinitionNumber = 0,
        fitBaseTypeId = 2,
        fieldName = "fuel_event",
        units = "code",
    )

    private val fuelTotalField = DeveloperField(
        fieldDefinitionNumber = 1,
        fitBaseTypeId = 132,
        fieldName = "fuel_total",
        units = "count",
    )

    override fun onCreate() {
        super.onCreate()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.connect { }
            karooSystem.consumerFlow<RideState>().collect { current ->
                when (current) {
                    is RideState.Idle -> FuelStore.onRideIdle(this@FuelTimerExtension)
                    is RideState.Recording -> FuelStore.onRideActive(this@FuelTimerExtension)
                    is RideState.Paused -> FuelStore.onRideActive(this@FuelTimerExtension)
                }
            }
        }
    }

    override fun startFit(emitter: Emitter<FitEffect>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            FuelStore.events.collect { event ->
                emitter.onNext(
                    WriteEventMesg(
                        event = 32,
                        eventType = 3,
                        values = listOf(
                            FieldValue(fuelCodeField, event.code.toDouble()),
                            FieldValue(fuelTotalField, event.total.toDouble()),
                        ),
                    ),
                )
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        serviceJob = null
        karooSystem.disconnect()
        super.onDestroy()
    }
}
