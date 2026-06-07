package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration131to132_DropTagSyncTime : Migration(131, 132) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tags_temp` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `color` INTEGER NOT NULL,
                `icon` TEXT,
                `orderNum` REAL NOT NULL,
                `dateTime` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `tags_temp` (
                `id`, `name`, `description`, `color`, `icon`, `orderNum`, `dateTime`, `isDeleted`
            )
            SELECT `id`, `name`, `description`, `color`, `icon`, `orderNum`, `dateTime`, `isDeleted`
            FROM `tags`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `tags`")
        db.execSQL("ALTER TABLE `tags_temp` RENAME TO `tags`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tags_association_temp` (
                `tagId` TEXT NOT NULL,
                `associatedId` TEXT NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`tagId`, `associatedId`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `tags_association_temp` (`tagId`, `associatedId`, `isDeleted`)
            SELECT `tagId`, `associatedId`, `isDeleted`
            FROM `tags_association`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `tags_association`")
        db.execSQL("ALTER TABLE `tags_association_temp` RENAME TO `tags_association`")
    }
}
