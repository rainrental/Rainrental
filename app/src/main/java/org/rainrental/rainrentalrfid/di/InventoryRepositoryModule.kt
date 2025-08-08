package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.commission.data.CommissionApi
import org.rainrental.rainrentalrfid.inventory.data.DefaultInventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object InventoryRepositoryModule {

    @Provides
    @Singleton
    fun providesInventoryRepository(commissionApi: CommissionApi, @Named("company_id") companyId: String): InventoryRepository{
        return DefaultInventoryRepository(commissionApi, companyId)
    }
}