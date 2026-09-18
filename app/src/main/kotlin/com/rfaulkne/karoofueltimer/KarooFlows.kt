package com.rfaulkne.karoofueltimer

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.KarooEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

inline fun <reified T : KarooEvent> KarooSystemService.consumerFlow(): Flow<T> {
    return callbackFlow {
        val listenerId = addConsumer<T> {
            trySend(it)
        }
        awaitClose {
            removeConsumer(listenerId)
        }
    }
}
