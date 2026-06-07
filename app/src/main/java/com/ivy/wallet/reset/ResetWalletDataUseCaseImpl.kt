package com.ivy.wallet.reset

import com.ivy.base.coroutines.ioThread
import com.ivy.data.DataObserver
import com.ivy.data.DataWriteEvent
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.ExchangeRatesRepository
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.TransactionRepository
import com.ivy.domain.preferences.AppPreferences
import com.ivy.domain.preferences.DataStorePreferencesRepository
import com.ivy.domain.usecase.ResetWalletDataUseCase
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.wallet.startup.InitialDataSetup
import javax.inject.Inject

class ResetWalletDataUseCaseImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val navigation: Navigation,
    private val dataObserver: DataObserver,
    private val dataStorePreferencesRepository: DataStorePreferencesRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val writeSettingsDao: WriteSettingsDao,
    private val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao,
    private val writeBudgetDao: WriteBudgetDao,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val initialDataSetup: InitialDataSetup,
) : ResetWalletDataUseCase {
    override suspend fun resetAllData() {
        ioThread {
            deleteAllData()
            dataStorePreferencesRepository.clearAll()
            appPreferences.clearAll()
        }

        initialDataSetup.setupDefaults(systemDarkMode = false)
        dataObserver.post(DataWriteEvent.AllDataChange)
        navigation.resetBackStack()
        navigation.navigateTo(MainScreen)
    }

    private suspend fun deleteAllData() {
        accountRepository.deleteAll()
        transactionRepository.deleteAll()
        categoryRepository.deleteAll()
        tagRepository.deleteAll()
        writeSettingsDao.deleteAll()
        writePlannedPaymentRuleDao.deleteAll()
        writeBudgetDao.deleteAll()
        writeLoanDao.deleteAll()
        writeLoanRecordDao.deleteAll()
        exchangeRatesRepository.deleteAll()
    }
}
