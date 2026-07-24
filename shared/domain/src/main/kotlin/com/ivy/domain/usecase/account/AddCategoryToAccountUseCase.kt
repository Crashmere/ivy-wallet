package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import javax.inject.Inject

/**
 * Adds [categoryId] to [accountId]'s own category list (idempotent). Used when a category is
 * picked from the "all categories" section or freshly created while recording on an account, so
 * it shows up by default for that account afterwards. Global category analytics are unaffected.
 */
class AddCategoryToAccountUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
) {
    suspend operator fun invoke(accountId: AccountId, categoryId: CategoryId) {
        val account = accountStore.findById(accountId) ?: return
        if (account.visibleCategories.contains(categoryId)) return
        accountStore.save(
            account.copy(visibleCategories = account.visibleCategories + categoryId)
        )
    }
}
