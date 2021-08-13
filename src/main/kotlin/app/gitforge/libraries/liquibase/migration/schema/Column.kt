package app.gitforge.libraries.liquibase.migration.schema

import app.gitforge.libraries.liquibase.migration.parser.Annotation
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

    var rawTypeName: String = ""

    companion object {
        fun getTypeByVmString(str: String): ColumnDataType {
            val type = when(str.replace("?", "").lowercase(Locale.getDefault())) {
                "boolean" -> BOOLEAN
                "string" -> STRING
                "long" -> LONG
                "int" -> INTEGER
                "integer" -> INTEGER
                "double" -> DOUBLE
                "localdate" -> DATE
                "localdatetime" -> DATETIME

                // handle literal types from kotlin
                "booleanliteral" -> BOOLEAN

                else -> FOREIGN_KEY
            }

            type.rawTypeName = str

            return type
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
    val nullable: Boolean? = null,
    var isPrimaryKey: Boolean? = null,
    var isUnique: Boolean? = null,
    var length: Int? = null,
) {
    companion object {
        fun fromYaml(constraint: YamlColumnConstraint?) : ColumnConstraint {

            if (constraint == null) {
                return ColumnConstraint()
            }

            return ColumnConstraint(
                constraint.nullable,
                constraint.primaryKey,
                constraint.unique,
                length = null
            )
        }
    }
}

data class Column(val name: String, var dataType: ColumnDataType, var constraints: ColumnConstraint, var annotations: List<Annotation>) {
    fun getColumnDataType() : String {
        val baseType = getColumnDataTypeStr()
        val length = constraints.length

        return if (length != null && length > 0) {
            "$baseType(${constraints.length})"
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