package com.ivy.data.api

import com.ivy.data.model.Theme

interface ThemeStore {
    suspend fun getTheme(fallback: Theme = Theme.AUTO): Theme

    suspend fun setTheme(theme: Theme): Theme
}
