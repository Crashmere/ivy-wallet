package com.ivy.domain.usecase.backup.github

/**
 * Carries the human-readable failure message produced by the GitHub backup store across the
 * use-case boundary (where results are exposed as [kotlin.Result] instead of Arrow's `Either`).
 */
class GitHubBackupException(message: String) : Exception(message)
