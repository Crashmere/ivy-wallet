package com.ivy.importdata.csvimport.flow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.R

@ExperimentalFoundationApi
@Composable
internal fun BoxWithConstraintsScope.ImportFrom(
    hasSkip: Boolean,

    onBack: () -> Unit,
    onSkip: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    onManualCsvImport: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        stickyHeader {
            ImportToolbar(
                hasSkip = hasSkip,
                onBack = onBack,
                onSkip = onSkip
            )
        }

        item {
            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(R.string.import_data),
                style = LegacyTheme.typo.h2.style(
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(Modifier.height(24.dp))

            ImportAction(
                title = stringResource(R.string.restore_backup_file),
                description = stringResource(R.string.restore_backup_file_description),
                onClick = onRestoreBackup
            )

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                onClick = onManualCsvImport
            ) {
                Text(
                    text = stringResource(id = R.string.manual_csv_import),
                    style = LegacyTheme.typo.b2.style(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            // last spacer
            Spacer(Modifier.height(96.dp))
        }
    }

    ImportGradientCutBottom(
        height = 96.dp
    )
}

@Composable
private fun BoxWithConstraintsScope.ImportGradientCutBottom(height: Dp = 96.dp) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, LegacyTheme.colors.pure)
                )
            )
            .align(Alignment.BottomCenter)
    )
}

@Composable
private fun ImportAction(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r3)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r3)
            .clickable {
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = title,
            style = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.Bold,
                color = LegacyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = description,
            style = LegacyTheme.typo.c.style(
                color = LegacyTheme.colors.pureInverse
            )
        )
    }
}
