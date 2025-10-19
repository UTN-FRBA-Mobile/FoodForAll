package ar.edu.utn.frba.mobile.foodforall.utils

import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkEvents {
    private val _intents = MutableSharedFlow<Intent>(replay = 0, extraBufferCapacity = 1)
    val intents = _intents.asSharedFlow()

    fun publish(intent: Intent) {
        _intents.tryEmit(intent)
    }
}