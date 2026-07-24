package com.ivy.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.icon.ItemIconMDefaultIcon
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.ui.compose.thenIf
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.theme.colors.suggestUniqueColor
import com.ivy.ui.theme.colors.dynamicContrast
import kotlinx.coroutines.launch

data class AccountModalSaveData(
    val name: String,
    val currency: String,
    val color: Int,
    val icon: String?,
    val balance: Double,
    val includeInBalance: Boolean,
    // The account's own category list, edited from the modal. null => leave unchanged
    // (e.g. account creation, where the list starts empty).
    val visibleCategoryIds: Set<UUID>? = null,
)

@Composable
fun BoxWithConstraintsScope.AccountModal(
    visible: Boolean,
    account: AccountModalAccount?,
    baseCurrency: String,
    balance: Double,
    adjustBalanceMode: Boolean = false,
    forceNonZeroBalance: Boolean = false,
    autoFocusKeyboard: Boolean = true,
    usedColors: List<Int> = emptyList(),
    categories: List<CategoryModalCategory> = emptyList(),
    onCreateAccount: (AccountModalSaveData) -> Unit,
    onEditAccount: (accountId: UUID, data: AccountModalSaveData) -> Unit,
    dismiss: () -> Unit,
) {
    var nameTextFieldValue by remember(visible, account) {
        mutableStateOf(selectEndTextFieldValue(account?.name))
    }
    var color by remember(visible, account) {
        mutableStateOf(account?.color?.let { Color(it) } ?: suggestUniqueColor(usedColors))
    }
    var amount by remember(visible, balance) {
        mutableStateOf(balance)
    }
    var currencyCode by remember(visible, account, baseCurrency) {
        mutableStateOf(account?.currency ?: baseCurrency)
    }
    var icon by remember(visible, account) {
        mutableStateOf(account?.icon)
    }
    var selectedCategoryIds by remember(visible, account) {
        mutableStateOf(account?.visibleCategoryIds ?: emptySet())
    }

    var amountModalVisible by remember { mutableStateOf(false) }
    var chooseIconModalVisible by remember(visible, account) {
        mutableStateOf(false)
    }
    val modalId = remember(visible, account, adjustBalanceMode) {
        if (visible) UUID.randomUUID() else null
    }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        shiftIfKeyboardShown = false,
        PrimaryAction = {
            val enabled = nameTextFieldValue.text.isNullOrBlank().not() &&
                (!forceNonZeroBalance || amount > 0)
            val onSave = {
                save(
                    account = account,
                    nameTextFieldValue = nameTextFieldValue,
                    currency = currencyCode,
                    color = color,
                    icon = icon,
                    amount = amount,
                    visibleCategoryIds = selectedCategoryIds.takeIf { account != null },

                    onCreateAccount = onCreateAccount,
                    onEditAccount = onEditAccount,
                    dismiss = dismiss
                )
            }
            if (account != null) {
                ModalSave(
                    enabled = enabled,
                    onClick = onSave
                )
            } else {
                ModalAdd(
                    enabled = enabled,
                    onClick = onSave
                )
            }
        }
    ) {
        onCompositionStart {
            if (adjustBalanceMode) {
                amountModalVisible = true
            }
        }

        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = if (account != null) {
                stringResource(
                    R.string.edit_account
                )
            } else {
                stringResource(R.string.new_account)
            },
        )

        Spacer(Modifier.height(24.dp))

        IconNameRow(
            hint = stringResource(R.string.account_name),
            defaultIcon = R.drawable.ic_custom_account_m,
            color = color,
            icon = icon,

            autoFocusKeyboard = autoFocusKeyboard,

            nameTextFieldValue = nameTextFieldValue,
            setNameTextFieldValue = { nameTextFieldValue = it },
            showChooseIconModal = {
                chooseIconModalVisible = true
            }
        )

        Spacer(Modifier.height(24.dp))

        AccountModalColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(modifier = Modifier.height(40.dp))

        ModalAmountSection(
            label = stringResource(R.string.enter_account_balance).uppercase(),
            currency = currencyCode,
            amount = amount,
            amountPaddingTop = 40.dp,
            amountPaddingBottom = 40.dp,
        ) {
            amountModalVisible = true
        }

        if (account != null && categories.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            AccountCategoriesSection(
                categories = categories,
                selectedIds = selectedCategoryIds,
                onToggle = { id ->
                    selectedCategoryIds = if (id in selectedCategoryIds) {
                        selectedCategoryIds - id
                    } else {
                        selectedCategoryIds + id
                    }
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    val amountModalId = remember(visible, amount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = currencyCode,
        initialAmount = amount,
        showPlusMinus = true,
        dismiss = { amountModalVisible = false }
    ) { newAmount ->
        amount = newAmount

        if (adjustBalanceMode) {
            save(
                account = account,
                nameTextFieldValue = nameTextFieldValue,
                currency = currencyCode,
                color = color,
                icon = icon,
                amount = newAmount,
                visibleCategoryIds = selectedCategoryIds.takeIf { account != null },

                onCreateAccount = onCreateAccount,
                onEditAccount = onEditAccount,
                dismiss = dismiss
            )
        }
    }

    ChooseIconModal(
        visible = chooseIconModalVisible,
        initialIcon = icon ?: "account",
        color = color,
        dismiss = { chooseIconModalVisible = false }
    ) {
        icon = it
    }
}

private fun save(
    account: AccountModalAccount?,
    nameTextFieldValue: TextFieldValue,
    currency: String,
    color: Color,
    icon: String?,
    amount: Double,
    visibleCategoryIds: Set<UUID>?,

    onCreateAccount: (AccountModalSaveData) -> Unit,
    onEditAccount: (accountId: UUID, data: AccountModalSaveData) -> Unit,
    dismiss: () -> Unit
) {
    val data = AccountModalSaveData(
        name = nameTextFieldValue.text.trim(),
        currency = currency,
        color = color.toArgb(),
        icon = icon,
        balance = amount,
        includeInBalance = true,
        visibleCategoryIds = visibleCategoryIds,
    )
    if (account != null) {
        onEditAccount(account.id, data)
    } else {
        onCreateAccount(data)
    }

    dismiss()
}

@Composable
private fun IvyNameTextField(
    modifier: Modifier = Modifier,
    underlineModifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: Color = AccountModalTheme.colors.pureInverse,
    hint: String?,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrect = true,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
        capitalization = KeyboardCapitalization.Sentences
    ),
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    Column {
        val isEmpty = value.text.isBlank()

        Box(
            modifier = modifier,
            contentAlignment = Alignment.CenterStart
        ) {
            if (isEmpty && hint.isNullOrBlank().not()) {
                Text(
                    text = hint!!,
                    style = AccountModalTheme.typo.b2.copy(
                        color = AccountModalTheme.colors.gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                )
            }

            val view = LocalView.current
            BasicTextField(
                modifier = Modifier
                    .testTag("base_input")
                    .focusRequester(focusRequester),
                value = value,
                onValueChange = onValueChanged,
                textStyle = AccountModalTheme.typo.b1.copy(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                ),
                singleLine = false,
                cursorBrush = SolidColor(AccountModalTheme.colors.pureInverse),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions ?: KeyboardActions(
                    onDone = {
                        view.hideKeyboard()
                    }
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        Spacer(
            modifier = underlineModifier
                .fillMaxWidth()
                .height(2.dp)
                .background(AccountModalTheme.colors.medium, AccountModalTheme.shapes.rFull)
        )
    }
}

@Composable
private fun IconNameRow(
    hint: String,
    @androidx.annotation.DrawableRes defaultIcon: Int,
    color: Color,
    icon: String?,
    autoFocusKeyboard: Boolean,
    nameTextFieldValue: TextFieldValue,
    setNameTextFieldValue: (TextFieldValue) -> Unit,
    showChooseIconModal: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val nameFocus = remember { FocusRequester() }

        onCompositionStart {
            if (autoFocusKeyboard) {
                nameFocus.requestFocus()
            }
        }

        Spacer(Modifier.width(24.dp))

        ItemIconMDefaultIcon(
            modifier = Modifier
                .clip(CircleShape)
                .background(color, CircleShape)
                .clickable {
                    showChooseIconModal()
                }
                .testTag("modal_item_icon"),
            iconName = icon,
            tint = color.dynamicContrast(),
            defaultIcon = defaultIcon
        )

        val view = LocalView.current
        IvyNameTextField(
            modifier = Modifier
                .padding(start = 28.dp, end = 36.dp)
                .focusRequester(nameFocus),
            underlineModifier = Modifier.padding(start = 24.dp, end = 32.dp),
            value = nameTextFieldValue,
            hint = hint,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text,
                autoCorrect = true
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    view.hideKeyboard()
                }
            ),
        ) { newValue ->
            setNameTextFieldValue(newValue)
        }
    }
}

@Composable
private fun ColumnScope.AccountModalColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = stringResource(R.string.choose_color),
        style = AccountModalTheme.typo.b2.copy(
            color = AccountModalTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    val accountColors = remember(selectedColor) {
        val palette = AccountBaseColors + AccountVariantColors
        if (selectedColor in palette) palette else listOf(selectedColor) + palette
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    onCompositionStart {
        val selectedColorIndex = accountColors.indexOf(selectedColor)
        if (selectedColorIndex != -1) {
            coroutineScope.launch {
                listState.scrollToItem(
                    index = selectedColorIndex,
                    scrollOffset = 0
                )
            }
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {
        items(
            count = accountColors.size
        ) { index ->
            AccountColorItem(
                index = index,
                color = accountColors[index],
                selectedColor = selectedColor,
                onSelected = onColorSelected,
            )
        }
    }
}

@Composable
private fun AccountColorItem(
    index: Int,
    color: Color,
    selectedColor: Color,
    onSelected: (Color) -> Unit
) {
    val selected = color == selectedColor

    if (index == 0) {
        Spacer(Modifier.width(24.dp))
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
            .background(color, CircleShape)
            .thenIf(selected) {
                border(width = 4.dp, color = color.dynamicContrast(), CircleShape)
            }
            .clickable(onClick = {
                onSelected(color)
            })
            .testTag("color_item_${color.value}"),
        contentAlignment = Alignment.Center
    ) {
    }

    Spacer(Modifier.width(if (selected) 16.dp else 24.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.AccountCategoriesSection(
    categories: List<CategoryModalCategory>,
    selectedIds: Set<UUID>,
    onToggle: (UUID) -> Unit,
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = "该账户可用类别",
        style = AccountModalTheme.typo.b2.copy(
            color = AccountModalTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(4.dp))

    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = "勾选的类别会在该账户记账时默认显示，其余收在“全部类别”里",
        style = AccountModalTheme.typo.b2.copy(
            color = AccountModalTheme.colors.gray,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            CategoryToggleChip(
                category = category,
                selected = category.id in selectedIds,
                onClick = { onToggle(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryToggleChip(
    category: CategoryModalCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val categoryColor = Color(category.color)
    val textColor = if (selected) categoryColor.dynamicContrast() else AccountModalTheme.colors.pureInverse
    val medium = AccountModalTheme.colors.medium
    val rFull = AccountModalTheme.shapes.rFull

    Row(
        modifier = Modifier
            .clip(rFull)
            .thenIf(selected) {
                background(categoryColor, rFull)
            }
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(categoryColor, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = category.name,
            style = AccountModalTheme.typo.b2.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
            ),
        )
    }
}

private val Ivy = Color(0xFF6B4DFF)
private val Purple1 = Color(0xFFC34CFF)
private val Purple2 = Color(0xFFFF4CFF)
private val Blue = Color(0xFF4CC3FF)
private val Blue2 = Color(0xFF45E6E6)
private val Blue3 = Color(0xFF457BE6)
private val Green = Color(0xFF14CC9E)
private val Green2 = Color(0xFF45E67B)
private val Green3 = Color(0xFF96E645)
private val Green4 = Color(0xFFC7E62E)
private val Yellow = Color(0xFFFFEE33)
private val Orange = Color(0xFFF29F30)
private val Orange2 = Color(0xFFE67B45)
private val Orange3 = Color(0xFFFFC34C)
private val Red = Color(0xFFFF4060)
private val Red2 = Color(0xFFE62E2E)
private val Red3 = Color(0xFFFF4CA6)

private val IvyLight = Color(0xFFD5CCFF)
private val Purple1Light = Color(0xFFEECCFF)
private val Purple2Light = Color(0xFFFFBFFF)
private val BlueLight = Color(0xFFB3E6FF)
private val Blue2Light = Color(0xFFB3FFFF)
private val Blue3Light = Color(0xFFCCDDFF)
private val GreenLight = Color(0xFFAAF2E0)
private val Green2Light = Color(0xFF99FFBB)
private val Green3Light = Color(0xFFCCFF99)
private val Green4Light = Color(0xFFEEFF99)
private val YellowLight = Color(0xFFFFF799)
private val OrangeLight = Color(0xFFFFDEB3)
private val Orange2Light = Color(0xFFFFCCB3)
private val Orange3Light = Color(0xFFFFDC99)
private val RedLight = Color(0xFFFFCCD5)
private val Red2Light = Color(0xFFFFB3B3)
private val Red3Light = Color(0xFFFFCCE6)

private val IvyDark = Color(0xFF352680)
private val Purple1Dark = Color(0xFF622680)
private val Purple2Dark = Color(0xFF802680)
private val BlueDark = Color(0xFF266280)
private val Blue2Dark = Color(0xFF227373)
private val Blue3Dark = Color(0xFF223D73)
private val GreenDark = Color(0xFF0A664F)
private val Green2Dark = Color(0xFF22733D)
private val Green3Dark = Color(0xFF66804D)
private val Green4Dark = Color(0xFF637317)
private val YellowDark = Color(0xFF807719)
private val OrangeDark = Color(0xFF734B17)
private val Orange2Dark = Color(0xFF66371F)
private val Orange3Dark = Color(0xFF806226)
private val RedDark = Color(0xFF801919)
private val Red2Dark = Color(0xFF802030)
private val Red3Dark = Color(0xFF802653)

private val AccountBaseColors = listOf(
    Ivy, Purple1, Purple2, Blue, Blue2, Blue3,
    Green, Green2, Green3, Green4, Yellow,
    Orange, Orange2, Orange3, Red, Red2, Red3,
)

private val AccountVariantColors = listOf(
    IvyLight, Purple1Light, Purple2Light, BlueLight, Blue2Light, Blue3Light,
    GreenLight, Green2Light, Green3Light, Green4Light, YellowLight,
    OrangeLight, Orange2Light, Orange3Light, RedLight, Red2Light, Red3Light,
    IvyDark, Purple1Dark, Purple2Dark, BlueDark, Blue2Dark, Blue3Dark,
    GreenDark, Green2Dark, Green3Dark, Green4Dark, YellowDark,
    OrangeDark, Orange2Dark, Orange3Dark, RedDark, Red2Dark, Red3Dark,
)
