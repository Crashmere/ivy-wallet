package com.ivy.wallet.startup

import arrow.core.raise.either
import com.ivy.base.resource.ResourceProvider
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.SaveAccountUseCase
import com.ivy.domain.usecase.category.SaveCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.ui.R
import java.util.UUID
import javax.inject.Inject

class PreloadDataLogic @Inject constructor(
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val resourceProvider: ResourceProvider,
) {
    private var categoryOrderNum = 0.0

    suspend fun preloadAccounts() {
        val baseCurrency = getBaseCurrency()
        saveAccountUseCase(
            Account(
                id = AccountId(UUID.randomUUID()),
                name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.cash)),
                asset = baseCurrency,
                color = ColorInt(Green),
                icon = IconAsset.from("cash").getOrNull(),
                includeInBalance = true,
                orderNum = 0.0,
            )
        )
        saveAccountUseCase(
            Account(
                id = AccountId(UUID.randomUUID()),
                name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.bank)),
                asset = baseCurrency,
                color = ColorInt(IvyDark),
                icon = IconAsset.from("bank").getOrNull(),
                includeInBalance = true,
                orderNum = 1.0,
            )
        )
    }

    suspend fun preloadCategories() {
        categoryOrderNum = 0.0

        val categoriesToPreload = defaultCategories()

        for (createData in categoriesToPreload) {
            preloadCategory(createData)
        }
    }

    private fun defaultCategories() = listOf(
        DefaultCategory(
            name = resourceProvider.getString(R.string.food_drinks),
            color = Green,
            icon = "fooddrink"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.bills_fees),
            color = Red,
            icon = "bills"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.transport),
            color = YellowLight,
            icon = "transport"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.groceries),
            color = GreenLight,
            icon = "groceries"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.entertainment),
            color = Orange,
            icon = "game"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.shopping),
            color = Ivy,
            icon = "shopping"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.gifts),
            color = RedLight,
            icon = "gift"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.health),
            color = IvyLight,
            icon = "health"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.investments),
            color = IvyDark,
            icon = "leaf"
        ),

        DefaultCategory(
            name = resourceProvider.getString(R.string.loans),
            color = BlueDark,
            icon = "loan"
        ),
    )

    private suspend fun preloadCategory(
        data: DefaultCategory,
    ) {
        val category: Category? = either {
            Category(
                name = NotBlankTrimmedString.from(data.name).bind(),
                color = ColorInt(data.color),
                icon = IconAsset.from(data.icon).getOrNull(),
                orderNum = categoryOrderNum++,
                id = CategoryId(UUID.randomUUID()),
            )
        }.getOrNull()

        if (category != null) {
            saveCategoryUseCase(category)
        }
    }
}

private data class DefaultCategory(
    val name: String,
    val color: Int,
    val icon: String,
)

private val Ivy = 0xFF6B4DFF.toInt()
private val IvyLight = 0xFFD5CCFF.toInt()
private val IvyDark = 0xFF352680.toInt()
private val BlueDark = 0xFF266280.toInt()
private val Green = 0xFF14CC9E.toInt()
private val GreenLight = 0xFFAAF2E0.toInt()
private val YellowLight = 0xFFFFF799.toInt()
private val Orange = 0xFFF29F30.toInt()
private val Red = 0xFFFF4060.toInt()
private val RedLight = 0xFFFFCCD5.toInt()
