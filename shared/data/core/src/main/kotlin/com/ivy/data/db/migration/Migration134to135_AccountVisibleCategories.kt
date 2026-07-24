package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the per-account "own category list" column and seeds it from history:
 * every account is assigned the (non-transfer) categories it has actually been used with.
 * Accounts with no categorized transactions are left null (no categories assigned yet).
 * UUIDs and [com.ivy.data.model.TransactionType] are stored as TEXT, so a comma-separated
 * `group_concat` matches the app's serialization (see [com.ivy.data.model.Budget.serialize]).
 */
internal class Migration134to135_AccountVisibleCategories : Migration(134, 135) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE accounts ADD COLUMN visibleCategoryIdsSerialized TEXT")
        database.execSQL(
            """
            UPDATE accounts SET visibleCategoryIdsSerialized = (
                SELECT group_concat(categoryId) FROM (
                    SELECT DISTINCT t.categoryId
                    FROM transactions t
                    WHERE t.accountId = accounts.id
                      AND t.categoryId IS NOT NULL
                      AND t.isDeleted = 0
                      AND t.type != 'TRANSFER'
                )
            )
            """.trimIndent()
        )
    }
}
