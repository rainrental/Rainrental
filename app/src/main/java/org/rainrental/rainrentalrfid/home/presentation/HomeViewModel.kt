package org.rainrental.rainrentalrfid.home.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig
) : ViewModel() {
    
    private val _showLogoAnimation = MutableStateFlow(false)
    val showLogoAnimation: StateFlow<Boolean> = _showLogoAnimation.asStateFlow()
    
    private val _logoAnimationComplete = MutableStateFlow(false)
    val logoAnimationComplete: StateFlow<Boolean> = _logoAnimationComplete.asStateFlow()
    
    init {
        // Check if logo animation has been shown in this app session
        val hasBeenShown = appConfig.isLogoAnimationShown(context)
        if (!hasBeenShown) {
            _showLogoAnimation.value = true
            startLogoAnimation()
        } else {
            _logoAnimationComplete.value = true
        }
    }
    
    private fun startLogoAnimation() {
        viewModelScope.launch {
            // Show logo for 2 seconds
            delay(2000)
            
            // Mark animation as complete
            _logoAnimationComplete.value = true
            
            // Mark as shown in preferences
            appConfig.setLogoAnimationShown(context, true)
        }
    }
    
    fun resetLogoAnimation() {
        // Reset for testing or if needed
        appConfig.setLogoAnimationShown(context, false)
        _showLogoAnimation.value = true
        _logoAnimationComplete.value = false
        startLogoAnimation()
    }
}
