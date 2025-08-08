package org.rainrental.rainrentalrfid.chainway.data

import android.content.Context
import com.rscja.scanner.utility.ScannerUtility
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class ResetModuleManager @Inject constructor(
    private val scannerUtility: ScannerUtility,
    @ApplicationContext private val context: Context
) : Logger
{
    suspend fun resetModule() : Boolean = coroutineScope{
        val waitForModuleReset = this.async {
            scannerUtility.close(context)
            scannerUtility.open(context)
            scannerUtility.resetScan(context)
            scannerUtility.disableFunction(context,(ScannerUtility.FUNCTION_2D))
            scannerUtility.disableFunction(context,(ScannerUtility.FUNCTION_2D_H))
            scannerUtility.disableFunction(context,(ScannerUtility.FUNCTION_UHF))
            scannerUtility.enableAuxiliaryLight(context,true)
            scannerUtility.enablePlayFailureSound(context,false)
            scannerUtility.enablePlaySuccessSound(context,false)
            val unusedKeyCode = 104
//            scannerUtility.setScanKey(context,0, intArrayOf(unusedKeyCode, unusedKeyCode,unusedKeyCode)) // d
            scannerUtility.setScanKey(context,0, intArrayOf(unusedKeyCode, unusedKeyCode))
            scannerUtility.setScanKey(context,1, intArrayOf(unusedKeyCode,unusedKeyCode))
            scannerUtility.setScanKey(context,2, intArrayOf(unusedKeyCode,unusedKeyCode))
            scannerUtility.setScanKey(context,3, intArrayOf(unusedKeyCode,unusedKeyCode))
            scannerUtility.close(context)
            true
        }
        return@coroutineScope waitForModuleReset.await()
    }
}