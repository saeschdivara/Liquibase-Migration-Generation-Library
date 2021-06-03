package app.gitforge.libraries.liquibase.migration.schema

import app.gitforge.libraries.liquibase.migration.yml.ColumnConstraint as YamlColumnConstraint

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

data class ColumnConstraint(
    val nullable: Boolean,
    var isPrimaryKey: Boolean = false,
    var isUnique: Boolean = false,
    var lenght: Int? = 0,
) {
    companion object {
        fun fromYaml(constraint: YamlColumnConstraint) : ColumnConstraint {
            return ColumnConstraint(
                constraint.nullable ?: !constraint.primaryKey,
                constraint.primaryKey,
                constraint.unique
            )
        }
    }
}

data class Column(val name: String, val dataType: ColumnDataType, val constraints: ColumnConstraint)