package org.rainrental.rainrentalrfid.di

import android.app.Application
import android.content.Context
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.scanner.utility.ScannerUtility
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.chainway.data.ChainwayRfidManager
import org.rainrental.rainrentalrfid.chainway.data.ChainwayScannerManager
import org.rainrental.rainrentalrfid.chainway.data.ResetModuleManager
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChainwayModule {

    @Provides
    @Singleton
    fun provideRFIDWithUHFUART(): RFIDWithUHFUART {
        return RFIDWithUHFUART.getInstance()
    }

    @Provides
    fun providesBarcodeDecoder() : BarcodeDecoder {
        return BarcodeFactory.getInstance().barcodeDecoder
    }

    @Provides
    @Singleton
    fun providesScannerManager(@ApplicationContext context: Context, resetModuleManager: ResetModuleManager, barcodeScanner: BarcodeDecoder, application: Application) : ScannerManager{
        return ChainwayScannerManager(context, resetModuleManager = resetModuleManager, barcodeScanner = barcodeScanner, application = application)
    }

    @Provides
    @Singleton
    fun providesRfidManager() : RfidManager {
        return ChainwayRfidManager
    }

    @Provides
    @Singleton
    fun providesScannerUtility(): ScannerUtility {
        return ScannerUtility.getScannerInerface()
    }

}

//@Module
//@InstallIn(SingletonComponent::class)
//object ScannerManagerModule{
//

//    @Provides
//    @Singleton
//    fun providesScannerManager(
//        @ApplicationContext context: Context,
//        application: Application,
//        barcodeDecoder: BarcodeDecoder,
////        moduleResetter: ModuleResetter
//    ) : ScannerManager {
//        return ScannerManager(
//            context,
//            application,
//            barcodeDecoder,
////            moduleResetter
//        )
//    }
//}
