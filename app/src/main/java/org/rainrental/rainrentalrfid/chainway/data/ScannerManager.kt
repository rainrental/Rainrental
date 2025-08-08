package org.rainrental.rainrentalrfid.chainway.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeDecoder.DECODE_SUCCESS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface ScannerManager : LifecycleEventObserver, Logger {
    fun initiliseScanner(lifecycle: Lifecycle)
    fun isConnected() : Boolean
    fun connect(source: LifecycleOwner) : Boolean
    fun disconnect(source: LifecycleOwner)
    suspend fun getBarcode(): Result<String,InputError>
    val barcodeHardwareState: StateFlow<BarcodeHardwareState>
}

@Singleton
class ChainwayScannerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val application:Application,
    private val resetModuleManager: ResetModuleManager,
    private var barcodeScanner : BarcodeDecoder
) : ScannerManager {

    private var isConnected: Boolean = false

    private val _barcodeHardwareState = MutableStateFlow(BarcodeHardwareState.Startup)
    override val barcodeHardwareState: StateFlow<BarcodeHardwareState> = _barcodeHardwareState.asStateFlow()

    override fun initiliseScanner(lifecycle: Lifecycle) {
        lifecycle.coroutineScope.launch {
            resetModuleManager.resetModule()
        }
    }

    override fun isConnected(): Boolean {
        return isConnected
    }

    private fun reopenScanner(source: LifecycleOwner) {
        application.mainExecutor.execute {
            if (!barcodeScanner.isOpen) {
                if (connect(source)){
                    _barcodeHardwareState.update { BarcodeHardwareState.Ready }
                }else{
                    _barcodeHardwareState.update { BarcodeHardwareState.Error }
                }
            }
        }
    }

    override fun connect(source: LifecycleOwner) : Boolean{
        val didOpen = barcodeScanner.open(context)
        if (didOpen){
            barcodeScanner.setParameter(764,10)
            barcodeScanner.setParameter(298,1)
            return true
        }else{
            loge("Error opening scanner")
            return false
        }
    }

    override fun disconnect(source: LifecycleOwner) {
        _barcodeHardwareState.update { BarcodeHardwareState.Busy }
        application.mainExecutor.execute {
            if (barcodeScanner.isOpen) barcodeScanner.stopScan()
            barcodeScanner.close()
            _barcodeHardwareState.update { BarcodeHardwareState.Sleeping }
        }
        isConnected = false
    }

    override suspend fun getBarcode(): Result<String, InputError> = suspendCoroutine { continuation ->
        var hasResumed = false
        
        try{
        barcodeScanner.setDecodeCallback { barcode->
            try{
                if (!hasResumed) {
                    if (barcode.resultCode == DECODE_SUCCESS){
                        logd(barcode.barcodeData)
                        _barcodeHardwareState.update { BarcodeHardwareState.Ready }
                        hasResumed = true
                        continuation.resume(Result.Success(barcode.barcodeData))
                    }else{
                        _barcodeHardwareState.update { BarcodeHardwareState.Ready }
                        hasResumed = true
                        continuation.resume(Result.Error(InputError.NoBarcode))
                    }
                }
            }catch (e:Exception){
                if (!hasResumed) {
                    loge("Barcode exception: $e")
                    _barcodeHardwareState.update { BarcodeHardwareState.Ready }
                    hasResumed = true
                    continuation.resume(Result.Error(InputError.HardwareError))
                }
            }
        }

            _barcodeHardwareState.update { BarcodeHardwareState.Busy }
            barcodeScanner.startScan()

        }catch (e:Exception){
            if (!hasResumed) {
                _barcodeHardwareState.update { BarcodeHardwareState.Ready }
                hasResumed = true
                continuation.resume(Result.Error(InputError.LifecycleError))
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event){
            Lifecycle.Event.ON_RESUME -> reopenScanner(source)
            Lifecycle.Event.ON_PAUSE -> disconnect(source)
            else -> {}
        }
    }

}