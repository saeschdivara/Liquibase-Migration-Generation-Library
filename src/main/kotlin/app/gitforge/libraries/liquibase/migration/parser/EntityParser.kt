package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.Table

interface EntityParser {
    fun getTableFromEntityClass(filePath: String): Table?

    fun getTableStyleName(klassName: String) : String {
        var tableName = ""
        var isFirstChar = true

        // translate klass style to table style
        for (c in klassName) {
            if (isFirstChar) {
                isFirstChar = false
                tableName = "${c.toLowerCase()}"
            }
            else if (c.isUpperCase()) {
                tableName += "_${c.toLowerCase()}"
            } else {
                tableName += c
            }
        }

        return tableName
    }
}