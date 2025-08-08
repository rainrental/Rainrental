package org.rainrental.rainrentalrfid.hardware

/**
 * Interface for ViewModels to implement to handle hardware button events.
 * ViewModels only need to implement the methods they care about.
 */
interface HardwareEventListener {
    
    /**
     * Called when the trigger button is pressed down
     */
    fun onTriggerDown() {}
    
    /**
     * Called when the trigger button is released
     */
    fun onTriggerUp() {}
    
    /**
     * Called when the side button is pressed down
     */
    fun onSideKeyDown() {}
    
    /**
     * Called when the side button is released
     */
    fun onSideKeyUp() {}
    
    /**
     * Called when the aux button is pressed down
     */
    fun onAuxKeyDown() {}
    
    /**
     * Called when the aux button is released
     */
    fun onAuxKeyUp() {}
} 