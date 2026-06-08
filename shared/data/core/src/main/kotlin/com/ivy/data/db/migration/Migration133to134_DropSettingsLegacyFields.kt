package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal class Migration133to134_DropSettingsLegacyFields : Migration(133, 134) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `settings_temp_without_legacy_fields` (
                `theme` TEXT NOT NULL,
                `currency` TEXT NOT NULL,
                `bufferAmount` REAL NOT NULL,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `settings_temp_without_legacy_fields` (
                `theme`, `currency`, `bufferAmount`, `id`
            )
            SELECT `theme`, `currency`, `bufferAmount`, `id`
            FROM `settings`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `settings`")
        db.execSQL("ALTER TABLE `settings_temp_without_legacy_fields` RENAME TO `settings`")
    }
}
