package org.rainrental.rainrentalrfid.hardware

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareEventBus @Inject constructor(
    private val appConfig: AppConfig
) {
    
    private val listeners = mutableSetOf<HardwareEventListener>()
    
    // Track button states to prevent repeated events
    private var triggerDown = false
    private var sideDown = false
    private var auxDown = false
    
    fun registerListener(listener: HardwareEventListener) {
        listeners.add(listener)
        Log.d("HardwareEventBus", "Registered listener: ${listener.javaClass.simpleName}, total listeners: ${listeners.size}")
    }
    
    fun unregisterListener(listener: HardwareEventListener) {
        listeners.remove(listener)
        Log.d("HardwareEventBus", "Unregistered listener: ${listener.javaClass.simpleName}, total listeners: ${listeners.size}")
    }
    
    fun onKeyDown(keyCode: Int) {
        Log.d("HardwareEventBus", "onKeyDown called with keyCode: $keyCode")
        when (keyCode) {
            TRIGGER_KEY_CODE -> {
                if (!triggerDown) {
                    triggerDown = true
                    Log.d("HardwareEventBus", "Trigger key down - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onTriggerDown on ${listener.javaClass.simpleName}")
                        listener.onTriggerDown()
                    }
                } else {
                    Log.d("HardwareEventBus", "Trigger key already down, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_LEFT -> {
                if (!sideDown) {
                    sideDown = true
                    Log.d("HardwareEventBus", "Left side key down - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onSideKeyDown on ${listener.javaClass.simpleName}")
                        listener.onSideKeyDown()
                    }
                } else {
                    Log.d("HardwareEventBus", "Side key already down, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_RIGHT -> {
                // Check if right side key should be ignored
                if (appConfig.HardwareKeys.IGNORE_RIGHT_SIDE_KEY) {
                    Log.d("HardwareEventBus", "Right side key down - IGNORED due to IGNORE_RIGHT_SIDE_KEY flag")
                    return
                }
                if (!sideDown) {
                    sideDown = true
                    Log.d("HardwareEventBus", "Right side key down - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onSideKeyDown on ${listener.javaClass.simpleName}")
                        listener.onSideKeyDown()
                    }
                } else {
                    Log.d("HardwareEventBus", "Side key already down, ignoring repeated event")
                }
            }
            AUX_KEY_CODE -> {
                if (!auxDown) {
                    auxDown = true
                    Log.d("HardwareEventBus", "Aux key down - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onAuxKeyDown on ${listener.javaClass.simpleName}")
                        listener.onAuxKeyDown()
                    }
                } else {
                    Log.d("HardwareEventBus", "Aux key already down, ignoring repeated event")
                }
            }
            else -> {
                Log.d("HardwareEventBus", "Unknown keyCode: $keyCode")
            }
        }
    }
    
    fun onKeyUp(keyCode: Int) {
        Log.d("HardwareEventBus", "onKeyUp called with keyCode: $keyCode")
        when (keyCode) {
            TRIGGER_KEY_CODE -> {
                if (triggerDown) {
                    triggerDown = false
                    Log.d("HardwareEventBus", "Trigger key up - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onTriggerUp on ${listener.javaClass.simpleName}")
                        listener.onTriggerUp()
                    }
                } else {
                    Log.d("HardwareEventBus", "Trigger key already up, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_LEFT -> {
                if (sideDown) {
                    sideDown = false
                    Log.d("HardwareEventBus", "Left side key up - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onSideKeyUp on ${listener.javaClass.simpleName}")
                        listener.onSideKeyUp()
                    }
                } else {
                    Log.d("HardwareEventBus", "Side key already up, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_RIGHT -> {
                // Check if right side key should be ignored
                if (appConfig.HardwareKeys.IGNORE_RIGHT_SIDE_KEY) {
                    Log.d("HardwareEventBus", "Right side key up - IGNORED due to IGNORE_RIGHT_SIDE_KEY flag")
                    return
                }
                if (sideDown) {
                    sideDown = false
                    Log.d("HardwareEventBus", "Right side key up - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onSideKeyUp on ${listener.javaClass.simpleName}")
                        listener.onSideKeyUp()
                    }
                } else {
                    Log.d("HardwareEventBus", "Side key already up, ignoring repeated event")
                }
            }
            AUX_KEY_CODE -> {
                if (auxDown) {
                    auxDown = false
                    Log.d("HardwareEventBus", "Aux key up - notifying ${listeners.size} listeners")
                    listeners.forEach { listener ->
                        Log.d("HardwareEventBus", "Calling onAuxKeyUp on ${listener.javaClass.simpleName}")
                        listener.onAuxKeyUp()
                    }
                } else {
                    Log.d("HardwareEventBus", "Aux key already up, ignoring repeated event")
                }
            }
            else -> {
                Log.d("HardwareEventBus", "Unknown keyCode: $keyCode")
            }
        }
    }
    
    companion object {
        const val TRIGGER_KEY_CODE = 293 // Actual trigger key code
        const val SIDE_KEY_CODE_LEFT = 139 // Left side button
        const val SIDE_KEY_CODE_RIGHT = 294 // Right side button  
        const val AUX_KEY_CODE = 142 // Actual aux key code
    }
} 