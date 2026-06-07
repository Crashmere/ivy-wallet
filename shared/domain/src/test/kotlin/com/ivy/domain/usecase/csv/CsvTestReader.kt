package com.ivy.domain.usecase.csv

import com.opencsv.CSVReaderBuilder
import com.opencsv.validators.LineValidator
import com.opencsv.validators.RowValidator
import java.io.StringReader

class CsvTestReader {

    fun readCsv(csv: String): List<List<String>> {
        val csvReader = CSVReaderBuilder(StringReader(csv))
            .withLineValidator(object : LineValidator {
                override fun isValid(line: String?): Boolean {
                    return true
                }

                override fun validate(line: String?) = Unit
            })
            .withRowValidator(object : RowValidator {
                override fun isValid(row: Array<out String>?): Boolean {
                    return true
                }

                override fun validate(row: Array<out String>?) = Unit
            })
            .build()

        return csvReader.readAll()
            .map { it.toList() }
    }
}
