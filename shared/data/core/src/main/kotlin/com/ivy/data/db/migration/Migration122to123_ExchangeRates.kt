package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal class Migration122to123_ExchangeRates : Migration(122, 123) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE exchange_rates ADD COLUMN manualOverride INTEGER NOT NULL DEFAULT 0")
    }
}
