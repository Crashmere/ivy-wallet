package com.ivy.wallet.ui.applocked

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.ui.R
import com.ivy.wallet.platform.hasLockScreen
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.IvyButton

@SuppressLint("ComposeModifierMissing")
@Composable
@Suppress("LongMethod", "FunctionNaming")
fun BoxWithConstraintsScope.AppLockedScreen(
    onShowOSBiometricsModal: () -> Unit,
    onContinueWithoutAuthentication: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier
                .background(LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
                .padding(vertical = 12.dp)
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.app_locked),
            style = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.ExtraBold,
            )
        )

        Spacer(Modifier.weight(1f))

        Image(
            modifier = Modifier
                .size(width = 96.dp, height = 138.dp),
            painter = painterResource(id = R.drawable.ic_fingerprint),
            colorFilter = ColorFilter.tint(LegacyTheme.colors.medium),
            contentScale = ContentScale.FillBounds,
            contentDescription = "unlock icon"
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.authenticate_text),
            style = LegacyTheme.typo.b2.style(
                fontWeight = FontWeight.SemiBold,
                color = Gray
            )
        )

        Spacer(Modifier.height(24.dp))

        val latestOnContinueWithoutAuthentication by rememberUpdatedState(
            newValue = onContinueWithoutAuthentication
        )
        val latestOnShowOSBiometricsModal by rememberUpdatedState(onShowOSBiometricsModal)

        val context = LocalContext.current
        IvyButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.unlock),
            textStyle = LegacyTheme.typo.b2.style(
                color = White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            wrapContentMode = false
        ) {
            osAuthentication(
                context = context,
                onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
            )
        }
        Spacer(Modifier.height(24.dp))

        // To automatically launch the biometric screen on load of this composable
        LaunchedEffect(true) {
            osAuthentication(
                context = context,
                onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
            )
        }
    }
}

private fun osAuthentication(
    context: Context,
    onShowOSBiometricsModal: () -> Unit,
    onContinueWithoutAuthentication: () -> Unit
) {
    if (hasLockScreen(context)) {
        onShowOSBiometricsModal()
    } else {
        onContinueWithoutAuthentication()
    }
}
