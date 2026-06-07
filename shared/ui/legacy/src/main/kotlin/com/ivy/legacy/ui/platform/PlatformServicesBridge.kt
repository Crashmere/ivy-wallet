package com.ivy.legacy.ui.platform

import androidx.compose.runtime.Composable
import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.platform.LocalBuildInfoProvider
import com.ivy.ui.platform.LocalFileSharer

@Composable
fun buildInfoProvider(): BuildInfoProvider = LocalBuildInfoProvider.current

@Composable
fun fileSharer(): FileSharer = LocalFileSharer.current
