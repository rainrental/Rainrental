package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.commission.data.CommissionApi
import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.commission.data.DefaultCommissionRepository
import org.rainrental.rainrentalrfid.commission.data.DummyCommissionRepository
import org.rainrental.rainrentalrfid.rainrental.data.RainRentalApi
import android.content.Context
import org.rainrental.rainrentalrfid.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommissionRepositoryModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    @Named("company_id")
    fun provideCompanyId(context: Context): String {
        return context.getString(R.string.company_id)
    }

    @Provides
    @Singleton
//    @Named("Production")
    fun providesCommissionRepository(commissionApi: CommissionApi, rainRentalApi: RainRentalApi, @Named("company_id") companyId: String) : CommissionRepository{
        return DefaultCommissionRepository(commissionApi, rainRentalApi, companyId)
    }

    @Provides
    @Singleton
    @Named("Dummy")
    fun providesDummyCommissionRepository(commissionApi: CommissionApi) : CommissionRepository{
        return DummyCommissionRepository(commissionApi)
    }

}