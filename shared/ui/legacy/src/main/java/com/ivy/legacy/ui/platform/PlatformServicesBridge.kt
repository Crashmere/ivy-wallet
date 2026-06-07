package com.ivy.legacy.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.FileSharer

@Composable
fun buildInfoProvider(): BuildInfoProvider = LocalContext.current as BuildInfoProvider

@Composable
fun fileSharer(): FileSharer = LocalContext.current as FileSharer
