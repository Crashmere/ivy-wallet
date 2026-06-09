package com.ivy.importdata.csvimport.flow

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.importdata.ImportDataTheme
import com.ivy.importdata.csv.Spacer8
import com.ivy.data.model.importing.ImportResult
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White

@SuppressLint("ComposeModifierMissing")
@Composable
internal fun ImportResultUI(
    result: ImportResult,
    isManualCsvImport: Boolean = false,
    onTryAgain: (() -> Unit)? = null,
    onBack: () -> Unit,
    onManualCsvImport: (() -> Unit)? = null,
    onFinish: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))

        ImportBackButton(
            modifier = Modifier.padding(start = 20.dp)
        ) { onBack() }

        Spacer(Modifier.height(24.dp))

        val importSuccess = result.transactionsImported > 0 &&
                result.transactionsImported > result.rowsFound / 2
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = if (importSuccess) stringResource(R.string.success) else stringResource(R.string.failure),
            style = ImportDataTheme.typo.h2.copy(
                fontWeight = FontWeight.Black,
                color = if (importSuccess) ImportDataTheme.colors.pureInverse else ImportDataTheme.colors.red,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(32.dp))

        val successPercent = if (result.rowsFound > 0) {
            (result.transactionsImported / result.rowsFound.toDouble()) * 100
        } else {
            0.0
        }

        SuccessSectionUI(
            result = result,
            successPercent = successPercent,
        )

        ImportDividerLine(
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        FailedSectionUI(
            result = result,
            successPercent = successPercent,
        )

        // The current result screen only summarizes failed rows; detailed failed-row inspection is not wired.

        if (!isManualCsvImport) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.csv_import_failed),
                color = ImportDataTheme.colors.pureInverse,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp),
                onClick = { onManualCsvImport?.invoke() }
            ) {
                Text(
                    text = stringResource(id = R.string.manual_csv_import),
                    style = ImportDataTheme.typo.b2.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Spacer8()

        ImportGradientButton(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.finish),
            textColor = White,
            backgroundGradient = IvyGradients.Ivy,
            hasNext = true,
            enabled = true
        ) {
            onFinish()
        }

        if (onTryAgain != null) {
            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                onClick = onTryAgain,
                enabled = true
            ) {
                Text(text = stringResource(R.string.try_again))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SuccessSectionUI(
    result: ImportResult,
    successPercent: Double,
) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.imported),
            style = ImportDataTheme.typo.b1.copy(
                color = ImportDataTheme.colors.green,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = "${successPercent.format(2)}%",
            style = ImportDataTheme.typo.nH2.copy(
                color = ImportDataTheme.colors.pureInverse,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        )

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.transactions_imported, result.transactionsImported),
            style = ImportDataTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = ImportDataTheme.colors.gray,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.accounts_imported, result.accountsImported),
            style = ImportDataTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = ImportDataTheme.colors.gray,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.categories_imported, result.categoriesImported),
            style = ImportDataTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = ImportDataTheme.colors.gray,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FailedSectionUI(
    result: ImportResult,
    successPercent: Double,
) {
    Column {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.failed),
            style = ImportDataTheme.typo.b1.copy(
                fontWeight = FontWeight.Black,
                color = ImportDataTheme.colors.red,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = "${(100 - successPercent).format(2)}%",
            style = ImportDataTheme.typo.nH2.copy(
                color = ImportDataTheme.colors.pureInverse,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        )

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(
                R.string.rows_from_csv_not_recognized,
                result.rowsFound - result.transactionsImported
            ),
            style = ImportDataTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = ImportDataTheme.colors.gray,
                textAlign = TextAlign.Start
            )
        )
    }
}
