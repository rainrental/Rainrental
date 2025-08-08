package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.commission.data.CommissionApi
import org.rainrental.rainrentalrfid.hunt.data.DefaultHuntRepository
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object HuntRepositoryModule {

    @Provides
    @Singleton
    fun providesHuntRepository(commissionApi: CommissionApi, @Named("company_id") companyId: String) : HuntRepository{
        return DefaultHuntRepository(commissionApi = commissionApi, companyId = companyId)
    }
}