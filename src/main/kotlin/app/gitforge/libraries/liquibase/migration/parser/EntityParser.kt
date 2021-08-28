package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.EmbeddedKey
import app.gitforge.libraries.liquibase.migration.schema.Table

data class ParsingResult(var tables: List<Table> = ArrayList(), var embeddedKeys: List<EmbeddedKey> = ArrayList())

interface EntityParser {
    fun parse(filePath: String): ParsingResult

    fun getTableStyleName(klassName: String) : String {
        var tableName = ""
        var isFirstChar = true

        // translate klass style to table style
        for (c in klassName) {
            if (isFirstChar) {
                isFirstChar = false
                tableName = "${c.lowercaseChar()}"
            }
            else if (c.isUpperCase()) {
                tableName += "_${c.lowercaseChar()}"
            } else {
                tableName += c
            }
        }

        return tableName
    }
}