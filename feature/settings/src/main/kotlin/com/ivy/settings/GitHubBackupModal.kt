package com.ivy.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalSave
import com.ivy.ui.modal.ModalTitle
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.GitHubBackupModal(
    visible: Boolean,
    initialConfig: GitHubBackupConfig?,
    onSave: (GitHubBackupConfig) -> Unit,
    onClear: () -> Unit,
    onTest: (GitHubBackupConfig) -> Unit,
    dismiss: () -> Unit,
) {
    val modalId = remember(visible, initialConfig) {
        if (visible) UUID.randomUUID() else null
    }

    var token by remember(visible, initialConfig) {
        mutableStateOf(initialConfig?.token.orEmpty())
    }
    var owner by remember(visible, initialConfig) {
        mutableStateOf(initialConfig?.owner.orEmpty())
    }
    var repo by remember(visible, initialConfig) {
        mutableStateOf(initialConfig?.repo.orEmpty())
    }
    var branch by remember(visible, initialConfig) {
        mutableStateOf(initialConfig?.branch ?: GitHubBackupConfig.DEFAULT_BRANCH)
    }
    var path by remember(visible, initialConfig) {
        mutableStateOf(initialConfig?.path ?: GitHubBackupConfig.DEFAULT_PATH)
    }

    fun buildConfig() = GitHubBackupConfig(
        token = token.trim(),
        owner = owner.trim(),
        repo = repo.trim(),
        branch = branch.trim().ifBlank { GitHubBackupConfig.DEFAULT_BRANCH },
        path = path.trim().ifBlank { GitHubBackupConfig.DEFAULT_PATH },
    )

    val valid = token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank()

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        SecondaryActions = {
            GitHubTestConnectionButton(enabled = valid) {
                onTest(buildConfig())
            }
        },
        PrimaryAction = {
            ModalSave(enabled = valid) {
                onSave(buildConfig())
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = "GitHub 云备份")

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "将全部数据快照保存到一个私有仓库。每次备份都会生成一次提交，" +
                "你可以在仓库的提交历史里查看每个版本。Token 等信息仅保存在本机。",
            style = SettingsTheme.typo.nB2.copy(
                color = SettingsTheme.colors.gray,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(24.dp))

        GitHubInputField(
            label = "Access Token",
            value = token,
            hint = "ghp_… / github_pat_…",
            isSecret = true,
            keyboardType = KeyboardType.Password,
            onValueChange = { token = it }
        )

        GitHubInputField(
            label = "Owner（用户名或组织）",
            value = owner,
            hint = "your-github-name",
            onValueChange = { owner = it }
        )

        GitHubInputField(
            label = "仓库名 Repository",
            value = repo,
            hint = "ivy-wallet-backup",
            onValueChange = { repo = it }
        )

        GitHubInputField(
            label = "分支 Branch",
            value = branch,
            hint = GitHubBackupConfig.DEFAULT_BRANCH,
            onValueChange = { branch = it }
        )

        GitHubInputField(
            label = "文件路径 Path",
            value = path,
            hint = GitHubBackupConfig.DEFAULT_PATH,
            imeAction = ImeAction.Done,
            onValueChange = { path = it }
        )

        if (initialConfig != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(SettingsTheme.shapes.rFull)
                    .clickable {
                        onClear()
                        dismiss()
                    }
                    .padding(vertical = 8.dp),
                text = "清除配置",
                style = SettingsTheme.typo.b2.copy(
                    color = SettingsTheme.colors.red,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun GitHubInputField(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
    isSecret: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = label,
            style = SettingsTheme.typo.nC.copy(
                color = SettingsTheme.colors.gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(6.dp))

        Box(contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    style = SettingsTheme.typo.b2.copy(
                        color = SettingsTheme.colors.gray,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                )
            }

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = SettingsTheme.typo.b2.copy(
                    color = SettingsTheme.colors.pureInverse,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(SettingsTheme.colors.pureInverse),
                visualTransformation = if (isSecret) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(SettingsTheme.colors.medium, SettingsTheme.shapes.rFull)
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun GitHubTestConnectionButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Spacer(Modifier.width(12.dp))

    Text(
        modifier = Modifier
            .clip(CircleShape)
            .border(2.dp, SettingsTheme.colors.medium, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        text = "测试连接",
        style = SettingsTheme.typo.b2.copy(
            color = if (enabled) SettingsTheme.colors.pureInverse else SettingsTheme.colors.gray,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
    )
}
