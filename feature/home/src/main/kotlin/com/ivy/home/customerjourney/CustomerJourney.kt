package com.ivy.home.customerjourney

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.R
import com.ivy.ui.theme.colors.Gradient
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.theme.colors.dynamicContrast
import com.ivy.ui.theme.colors.findContrastTextColor
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CustomerJourney(
    customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    modifier: Modifier = Modifier,
    onDismiss: (CustomerJourneyCardModel) -> Unit,
    onAction: (CustomerJourneyAction) -> Unit,
) {
    if (customerJourneyCards.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
    }

    for (card in customerJourneyCards) {
        Spacer(Modifier.height(12.dp))

        CustomerJourneyCard(
            modifier = modifier,
            cardData = card,
            onDismiss = {
                onDismiss(card)
            }
        ) {
            onAction(card.action)
        }
    }
}

@Composable
internal fun CustomerJourneyCard(
    cardData: CustomerJourneyCardModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onCTA: () -> Unit,
) {
    val backgroundColor = Color(cardData.backgroundColorArgb)
    val backgroundGradient = Gradient.solid(backgroundColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .drawColoredShadow(backgroundColor)
            .background(backgroundGradient.asHorizontalBrush(), LegacyTheme.shapes.r3)
            .clip(LegacyTheme.shapes.r3)
            .clickable {
                onCTA()
            }
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp, end = 16.dp),
                text = cardData.title,
                style = LegacyTheme.typo.b1.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = findContrastTextColor(backgroundColor),
                    textAlign = TextAlign.Start
                )
            )

            if (cardData.hasDismiss) {
                ResourceIcon(
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                        }
                        .padding(8.dp), // enlarge click area
                    icon = R.drawable.ic_dismiss,
                    tint = backgroundColor.dynamicContrast(),
                    contentDescription = "prompt_dismiss",
                )

                Spacer(Modifier.width(20.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 32.dp),
            text = cardData.description,
            style = LegacyTheme.typo.b2.copy(
                fontWeight = FontWeight.Medium,
                color = findContrastTextColor(backgroundColor),
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(32.dp))

        if (cardData.cta != null) {
            IvyButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 20.dp)
                    .testTag("cta_prompt_${cardData.id}"),
                text = cardData.cta,
                shadowAlpha = 0f,
                iconStart = cardData.ctaIcon,
                iconTint = backgroundColor,
                textStyle = LegacyTheme.typo.b2.copy(
                    color = backgroundColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                padding = 8.dp,
                backgroundGradient = Gradient.solid(findContrastTextColor(backgroundColor))
            ) {
                onCTA()
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
