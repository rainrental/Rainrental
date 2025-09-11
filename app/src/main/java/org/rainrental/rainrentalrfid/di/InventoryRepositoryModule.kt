package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.inventory.data.DefaultInventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.domain.StartInventoryAllUseCase
import org.rainrental.rainrentalrfid.inventory.domain.StopInventoryAllUseCase
import org.rainrental.rainrentalrfid.inventory.domain.LogInventoryAllUseCase
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

    @Provides
    @Singleton
    fun providesStartInventoryAllUseCase(inventoryRepository: InventoryRepository, dependencies: BaseViewModelDependencies): StartInventoryAllUseCase {
        return StartInventoryAllUseCase(inventoryRepository, dependencies)
    }

    @Provides
    @Singleton
    fun providesStopInventoryAllUseCase(inventoryRepository: InventoryRepository, dependencies: BaseViewModelDependencies): StopInventoryAllUseCase {
        return StopInventoryAllUseCase(inventoryRepository, dependencies)
    }

    @Provides
    @Singleton
    fun providesLogInventoryAllUseCase(inventoryRepository: InventoryRepository, dependencies: BaseViewModelDependencies): LogInventoryAllUseCase {
        return LogInventoryAllUseCase(inventoryRepository, dependencies)
    }
}