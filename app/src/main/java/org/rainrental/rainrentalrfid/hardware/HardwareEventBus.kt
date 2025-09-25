package org.rainrental.rainrentalrfid.hardware

import org.rainrental.rainrentalrfid.logging.LogUtils
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareEventBus @Inject constructor(
    private val appConfig: AppConfig
) {
    
    // Hybrid approach: Keep old system for backward compatibility, add active listener for screen-aware routing
    private val listeners = mutableSetOf<HardwareEventListener>()
    private var activeListener: HardwareEventListener? = null
    
    // Track button states to prevent repeated events
    private var triggerDown = false
    private var sideDown = false
    private var auxDown = false
    
    fun registerListener(listener: HardwareEventListener) {
        listeners.add(listener)
        LogUtils.logd("HardwareEventBus", "Registered listener: ${listener.javaClass.simpleName}, total listeners: ${listeners.size}")
    }
    
    fun unregisterListener(listener: HardwareEventListener) {
        listeners.remove(listener)
        LogUtils.logd("HardwareEventBus", "Unregistered listener: ${listener.javaClass.simpleName}, total listeners: ${listeners.size}")
    }
    
    /**
     * Set the active listener that will receive hardware events.
     * Only one listener can be active at a time.
     */
    fun setActiveListener(listener: HardwareEventListener?) {
        val previousListener = activeListener?.javaClass?.simpleName
        activeListener = listener
        val newListener = listener?.javaClass?.simpleName ?: "none"
        LogUtils.logd("HardwareEventBus", "🔥 Active listener changed: $previousListener -> $newListener")
    }
    
    /**
     * Get the currently active listener (for debugging)
     */
    fun getActiveListener(): HardwareEventListener? = activeListener
    
    fun onKeyDown(keyCode: Int) {
        LogUtils.logd("HardwareEventBus", "🔥 onKeyDown called with keyCode: $keyCode, active listener: ${activeListener?.javaClass?.simpleName ?: "none"}")
        
        when (keyCode) {
            TRIGGER_KEY_CODE -> {
                if (!triggerDown) {
                    triggerDown = true
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Trigger key down - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onTriggerDown()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Trigger key down - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onTriggerDown on ${listener.javaClass.simpleName}")
                            listener.onTriggerDown()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Trigger key already down, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_LEFT -> {
                if (!sideDown) {
                    sideDown = true
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Left side key down - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onSideKeyDown()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Left side key down - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onSideKeyDown on ${listener.javaClass.simpleName}")
                            listener.onSideKeyDown()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Side key already down, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_RIGHT -> {
                // Check if right side key should be ignored
                if (appConfig.HardwareKeys.IGNORE_RIGHT_SIDE_KEY) {
                    LogUtils.logd("HardwareEventBus", "Right side key down - IGNORED due to IGNORE_RIGHT_SIDE_KEY flag")
                    return
                }
                if (!sideDown) {
                    sideDown = true
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Right side key down - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onSideKeyDown()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Right side key down - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onSideKeyDown on ${listener.javaClass.simpleName}")
                            listener.onSideKeyDown()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Side key already down, ignoring repeated event")
                }
            }
            AUX_KEY_CODE -> {
                if (!auxDown) {
                    auxDown = true
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Aux key down - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onAuxKeyDown()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Aux key down - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onAuxKeyDown on ${listener.javaClass.simpleName}")
                            listener.onAuxKeyDown()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Aux key already down, ignoring repeated event")
                }
            }
            else -> {
                LogUtils.logd("HardwareEventBus", "Unknown keyCode: $keyCode")
            }
        }
    }
    
    fun onKeyUp(keyCode: Int) {
        LogUtils.logd("HardwareEventBus", "🔥 onKeyUp called with keyCode: $keyCode, active listener: ${activeListener?.javaClass?.simpleName ?: "none"}")
        
        when (keyCode) {
            TRIGGER_KEY_CODE -> {
                if (triggerDown) {
                    triggerDown = false
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Trigger key up - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onTriggerUp()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Trigger key up - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onTriggerUp on ${listener.javaClass.simpleName}")
                            listener.onTriggerUp()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Trigger key already up, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_LEFT -> {
                if (sideDown) {
                    sideDown = false
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Left side key up - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onSideKeyUp()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Left side key up - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onSideKeyUp on ${listener.javaClass.simpleName}")
                            listener.onSideKeyUp()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Side key already up, ignoring repeated event")
                }
            }
            SIDE_KEY_CODE_RIGHT -> {
                // Check if right side key should be ignored
                if (appConfig.HardwareKeys.IGNORE_RIGHT_SIDE_KEY) {
                    LogUtils.logd("HardwareEventBus", "Right side key up - IGNORED due to IGNORE_RIGHT_SIDE_KEY flag")
                    return
                }
                if (sideDown) {
                    sideDown = false
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Right side key up - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onSideKeyUp()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Right side key up - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onSideKeyUp on ${listener.javaClass.simpleName}")
                            listener.onSideKeyUp()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Side key already up, ignoring repeated event")
                }
            }
            AUX_KEY_CODE -> {
                if (auxDown) {
                    auxDown = false
                    if (activeListener != null) {
                        LogUtils.logd("HardwareEventBus", "Aux key up - notifying active listener: ${activeListener!!.javaClass.simpleName}")
                        activeListener!!.onAuxKeyUp()
                    } else {
                        LogUtils.logd("HardwareEventBus", "Aux key up - no active listener, notifying all ${listeners.size} listeners")
                        listeners.forEach { listener ->
                            LogUtils.logd("HardwareEventBus", "Calling onAuxKeyUp on ${listener.javaClass.simpleName}")
                            listener.onAuxKeyUp()
                        }
                    }
                } else {
                    LogUtils.logd("HardwareEventBus", "Aux key already up, ignoring repeated event")
                }
            }
            else -> {
                LogUtils.logd("HardwareEventBus", "Unknown keyCode: $keyCode")
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