package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration132to133_DropIsSynced : Migration(132, 133) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.dropIsSyncedColumn(
            tableName = "accounts",
            columns = listOf(
                "`name` TEXT NOT NULL",
                "`currency` TEXT",
                "`color` INTEGER NOT NULL",
                "`icon` TEXT",
                "`orderNum` REAL NOT NULL",
                "`includeInBalance` INTEGER NOT NULL",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "name", "currency", "color", "icon", "orderNum",
                "includeInBalance", "isDeleted", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "transactions",
            columns = listOf(
                "`accountId` TEXT NOT NULL",
                "`type` TEXT NOT NULL",
                "`amount` REAL NOT NULL",
                "`toAccountId` TEXT",
                "`toAmount` REAL",
                "`title` TEXT",
                "`description` TEXT",
                "`dateTime` INTEGER",
                "`categoryId` TEXT",
                "`dueDate` INTEGER",
                "`recurringRuleId` TEXT",
                "`paidForDateTime` INTEGER",
                "`attachmentUrl` TEXT",
                "`loanId` TEXT",
                "`loanRecordId` TEXT",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "accountId", "type", "amount", "toAccountId", "toAmount",
                "title", "description", "dateTime", "categoryId", "dueDate",
                "recurringRuleId", "paidForDateTime", "attachmentUrl", "loanId",
                "loanRecordId", "isDeleted", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "categories",
            columns = listOf(
                "`name` TEXT NOT NULL",
                "`color` INTEGER NOT NULL",
                "`icon` TEXT",
                "`orderNum` REAL NOT NULL",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "name", "color", "icon", "orderNum", "isDeleted", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "settings",
            columns = listOf(
                "`theme` TEXT NOT NULL",
                "`currency` TEXT NOT NULL",
                "`bufferAmount` REAL NOT NULL",
                "`name` TEXT NOT NULL",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "theme", "currency", "bufferAmount", "name", "isDeleted", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "planned_payment_rules",
            columns = listOf(
                "`startDate` INTEGER",
                "`intervalN` INTEGER",
                "`intervalType` TEXT",
                "`oneTime` INTEGER NOT NULL",
                "`type` TEXT NOT NULL",
                "`accountId` TEXT NOT NULL",
                "`amount` REAL NOT NULL",
                "`categoryId` TEXT",
                "`title` TEXT",
                "`description` TEXT",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "startDate", "intervalN", "intervalType", "oneTime", "type",
                "accountId", "amount", "categoryId", "title", "description",
                "isDeleted", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "budgets",
            columns = listOf(
                "`name` TEXT NOT NULL",
                "`amount` REAL NOT NULL",
                "`categoryIdsSerialized` TEXT",
                "`accountIdsSerialized` TEXT",
                "`isDeleted` INTEGER NOT NULL",
                "`orderId` REAL NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "name", "amount", "categoryIdsSerialized", "accountIdsSerialized",
                "isDeleted", "orderId", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "loans",
            columns = listOf(
                "`name` TEXT NOT NULL",
                "`amount` REAL NOT NULL",
                "`type` TEXT NOT NULL",
                "`color` INTEGER NOT NULL",
                "`icon` TEXT",
                "`orderNum` REAL NOT NULL",
                "`accountId` TEXT",
                "`note` TEXT",
                "`isDeleted` INTEGER NOT NULL",
                "`dateTime` INTEGER",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "name", "amount", "type", "color", "icon", "orderNum",
                "accountId", "note", "isDeleted", "dateTime", "id"
            )
        )

        db.dropIsSyncedColumn(
            tableName = "loan_records",
            columns = listOf(
                "`loanId` TEXT NOT NULL",
                "`amount` REAL NOT NULL",
                "`note` TEXT",
                "`dateTime` INTEGER NOT NULL",
                "`interest` INTEGER NOT NULL",
                "`accountId` TEXT",
                "`convertedAmount` REAL",
                "`loanRecordType` TEXT NOT NULL",
                "`isDeleted` INTEGER NOT NULL",
                "`id` TEXT NOT NULL",
            ),
            copiedColumns = listOf(
                "loanId", "amount", "note", "dateTime", "interest", "accountId",
                "convertedAmount", "loanRecordType", "isDeleted", "id"
            )
        )
    }

    private fun SupportSQLiteDatabase.dropIsSyncedColumn(
        tableName: String,
        columns: List<String>,
        copiedColumns: List<String>
    ) {
        val tempTableName = "${tableName}_temp_without_is_synced"
        val columnDefinitions = columns.joinToString(separator = ",\n")
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS `$tempTableName` (
                $columnDefinitions,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        val copiedColumnNames = copiedColumns.joinToString(separator = ", ") { "`$it`" }
        execSQL(
            """
            INSERT INTO `$tempTableName` ($copiedColumnNames)
            SELECT $copiedColumnNames FROM `$tableName`
            """.trimIndent()
        )
        execSQL("DROP TABLE `$tableName`")
        execSQL("ALTER TABLE `$tempTableName` RENAME TO `$tableName`")
    }
}
