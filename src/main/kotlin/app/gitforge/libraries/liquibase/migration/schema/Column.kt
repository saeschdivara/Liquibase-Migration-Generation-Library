package app.gitforge.libraries.liquibase.migration.schema

enum class ColumnDataType {
    BOOLEAN,
    STRING,
    LONG,
    INTEGER,
    DOUBLE,
    DATE,
    DATETIME,
    UNKNOWN;

    companion object {
        fun getTypeByKotlinString(str: String): ColumnDataType {
            return when(str.replace("?", "")) {
                "Boolean" -> BOOLEAN
                "String" -> STRING
                "Long" -> LONG
                "Int" -> INTEGER
                "Double" -> DOUBLE
                "LocalDate" -> DATE
                "LocalDateTime" -> DATETIME
                else -> UNKNOWN
            }
        }

        fun getTypeByMigrationString(str: String): ColumnDataType {
            val lowerStr = str.toLowerCase()

            if (lowerStr == "bigint") {
                return LONG
            }

            if (lowerStr.startsWith("varchar")) {
                return STRING
            }

            return UNKNOWN
        }
    }
}

data class Column(val name: String, val dataType: ColumnDataType, val nullable: Boolean)