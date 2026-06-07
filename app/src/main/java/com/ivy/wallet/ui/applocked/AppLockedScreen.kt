package com.ivy.wallet.ui.applocked

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.ui.R

@SuppressLint("ComposeModifierMissing")
@Composable
@Suppress("LongMethod", "FunctionNaming")
fun BoxWithConstraintsScope.AppLockedScreen(
    hasLockScreen: () -> Boolean,
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
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(percent = 50)
                )
                .padding(vertical = 12.dp)
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.app_locked),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(Modifier.weight(1f))

        Image(
            modifier = Modifier
                .size(width = 96.dp, height = 138.dp),
            painter = painterResource(id = R.drawable.ic_fingerprint),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.FillBounds,
            contentDescription = "unlock icon"
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(R.string.authenticate_text),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(24.dp))

        val latestOnContinueWithoutAuthentication by rememberUpdatedState(
            newValue = onContinueWithoutAuthentication
        )
        val latestOnShowOSBiometricsModal by rememberUpdatedState(onShowOSBiometricsModal)

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                osAuthentication(
                    hasLockScreen = hasLockScreen,
                    onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                    onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
                )
            }
        ) {
            Text(
                text = stringResource(R.string.unlock),
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(24.dp))

        // To automatically launch the biometric screen on load of this composable
        LaunchedEffect(true) {
            osAuthentication(
                hasLockScreen = hasLockScreen,
                onShowOSBiometricsModal = latestOnShowOSBiometricsModal,
                onContinueWithoutAuthentication = latestOnContinueWithoutAuthentication
            )
        }
    }
}

private fun osAuthentication(
    hasLockScreen: () -> Boolean,
    onShowOSBiometricsModal: () -> Unit,
    onContinueWithoutAuthentication: () -> Unit
) {
    if (hasLockScreen()) {
        onShowOSBiometricsModal()
    } else {
        onContinueWithoutAuthentication()
    }
}
