package app.gitforge.libraries.liquibase.migration.kotlin

class Annotation(val name: String, val parameters: List<AnnotationParameter>)

enum class ParameterType {
    STRING,
    STRING_ARRAY,
    INTEGER,
    INTEGER_ARRAY,
    BOOLEAN,
    BOOLEAN_ARRAY,
    UNKNOWN;

    companion object {
        fun getTypeFromString(nodeDescription: String): ParameterType {
            return when (nodeDescription) {
                "KlassString" -> STRING
                "IntegerLiteral" -> INTEGER
                "BooleanLiteral" -> BOOLEAN
                else -> UNKNOWN
            }
        }
    }
}

class AnnotationParameter(val name: String, val type: ParameterType) {
    var stringVal: String? = null
    var integerVal: Int? = null
    var booleanVal: Boolean? = null
}