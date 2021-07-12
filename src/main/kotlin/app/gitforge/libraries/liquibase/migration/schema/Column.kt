package app.gitforge.libraries.liquibase.migration.schema

import java.util.*
import app.gitforge.libraries.liquibase.migration.yml.ColumnConstraint as YamlColumnConstraint

enum class ColumnDataType {
    BOOLEAN,
    STRING,
    LONG,
    INTEGER,
    DOUBLE,
    DATE,
    DATETIME,
    FOREIGN_KEY;

    companion object {
        fun getTypeByVmString(str: String): ColumnDataType {
            return when(str.replace("?", "").lowercase(Locale.getDefault())) {
                "boolean" -> BOOLEAN
                "string" -> STRING
                "long" -> LONG
                "int" -> INTEGER
                "integer" -> INTEGER
                "double" -> DOUBLE
                "localdate" -> DATE
                "localdatetime" -> DATETIME

                // handle literal types from kotlin
                "BooleanLiteral" -> BOOLEAN

                else -> FOREIGN_KEY
            }
        }

        fun getTypeByMigrationString(str: String): ColumnDataType {
            val lowerStr = str.lowercase(Locale.getDefault())

            if (lowerStr == "bigint") {
                return LONG
            }

            if (lowerStr.startsWith("varchar")) {
                return STRING
            }

            return FOREIGN_KEY
        }
    }
}

data class ColumnConstraint(
    val nullable: Boolean,
    var isPrimaryKey: Boolean = false,
    var isUnique: Boolean = false,
    var lenght: Int = 0,
) {
    companion object {
        fun fromYaml(constraint: YamlColumnConstraint?) : ColumnConstraint {

            if (constraint == null) {
                return ColumnConstraint(true)
            }

            return ColumnConstraint(
                constraint.nullable ?: !constraint.primaryKey,
                constraint.primaryKey,
                constraint.unique
            )
        }
    }
}

data class Column(val name: String, var dataType: ColumnDataType, var constraints: ColumnConstraint) {
    fun getColumnDataType() : String {
        val baseType = getColumnDataTypeStr()

        return if (constraints.lenght > 0) {
            "$baseType(${constraints.lenght})"
        } else {
            baseType
        }
    }

    private fun getColumnDataTypeStr() : String {
        return when(dataType) {
            ColumnDataType.STRING -> "varchar"
            ColumnDataType.LONG -> "bigint"
            ColumnDataType.INTEGER -> "int"
            ColumnDataType.BOOLEAN -> "bool"
            ColumnDataType.DATE ->  "date"
            ColumnDataType.DATETIME ->  "datetime"
            ColumnDataType.DOUBLE -> "double"
            ColumnDataType.FOREIGN_KEY -> "foreign-key"
        }
    }
}