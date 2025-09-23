package org.rainrental.rainrentalrfid.navigation

/**
 * Interface for features that need back confirmation when state has changed
 */
interface BackConfirmableFeature {
    /**
     * Check if the feature has unsaved changes that would be lost
     * @return true if there are unsaved changes, false otherwise
     */
    fun hasUnsavedChanges(): Boolean
    
    /**
     * Reset the feature state to initial state
     */
    fun resetState()
    
    /**
     * Get a user-friendly description of what will be lost
     * @return description of unsaved changes
     */
    fun getUnsavedChangesDescription(): String
}
