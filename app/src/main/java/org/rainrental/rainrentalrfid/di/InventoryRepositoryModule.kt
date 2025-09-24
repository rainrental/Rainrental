package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.inventory.data.DefaultInventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object InventoryRepositoryModule {

    @Provides
    @Singleton
    fun providesInventoryRepository(backendApi: BackendApi, @Named("company_id") companyId: String): InventoryRepository{
        return DefaultInventoryRepository(backendApi, companyId)
    }

}