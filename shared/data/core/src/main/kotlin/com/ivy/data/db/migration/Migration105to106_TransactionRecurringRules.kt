package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration105to106_TransactionRecurringRules : Migration(105, 106) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val transactionRulesTable =
            "CREATE TABLE IF NOT EXISTS `transaction_recurring_rules` (`startDate` INTEGER NOT NULL, `intervalSeconds` INTEGER NOT NULL, `type` TEXT NOT NULL, `accountId` TEXT NOT NULL, `amount` REAL NOT NULL, `categoryId` TEXT, `title` TEXT, `description` TEXT, `id` TEXT NOT NULL, PRIMARY KEY(`id`))"
        database.execSQL(transactionRulesTable)

        database.execSQL("ALTER TABLE transactions ADD COLUMN recurringRuleId TEXT")
    }
}
