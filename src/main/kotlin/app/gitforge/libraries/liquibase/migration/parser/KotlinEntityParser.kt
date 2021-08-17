package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.Column
import app.gitforge.libraries.liquibase.migration.schema.ColumnConstraint
import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import app.gitforge.libraries.liquibase.migration.schema.Table
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.ast.DefaultAstTerminal
import kotlinx.ast.common.klass.*
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import mu.KotlinLogging

object KotlinEntityParser : EntityParser {

    private val logger = KotlinLogging.logger {}

    override fun getTableFromEntityClass(filePath: String): Table? {
        val stringSource = AstSource.File(filePath)
        val ast = KotlinGrammarAntlrKotlinParser.parseKotlinFile(stringSource)
        var table: Table? = null
        ast.summary(false).onSuccess { list ->
            run {
                for (klass in list.filterIsInstance(KlassDeclaration::class.java)) {
                    val tableAnnotation = getTableAnnotation(klass)
                    if (tableAnnotation != null) {
                        val tableName = getTableName(klass, tableAnnotation)
                        val columns = getTableColumns(klass)

                        table = Table(tableName, klass.identifier?.rawName ?: "", columns)
                        return@run
                    }
                }
            }
        }

        return table
    }

    private fun getTableAnnotation(klass: KlassDeclaration): KlassAnnotation? {
        return klass.annotations.find { annotation -> annotation.identifier.first().identifier == "Entity" }
    }

    private fun getTableName(klass: KlassDeclaration, annotation: KlassAnnotation): String {

        var klassName = ""
        if (annotation.arguments.isEmpty()) {
            klassName = klass.identifier?.rawName ?: klassName
        } else {
            val parsedAnnotation = getAnnotation(annotation)
            klassName = parsedAnnotation.parameters.find { parameter -> parameter.name == "name" }?.stringVal ?: klassName
        }

        return getTableStyleName(klassName)
    }

    private fun getTableColumns(klass: KlassDeclaration): MutableList<Column> {
        val columns = ArrayList<Column>()
        if (klass.parameter.isNotEmpty()) {
            val constructor = klass.parameter.first()
            val parameters = constructor.children.filterIsInstance(KlassDeclaration::class.java)

            for (parameter in parameters) {
                val column = getTableColumnFromDeclaration(parameter)

                if (column != null) {
                    columns.add(column)
                }
            }
        }

        val possibleClassBodies = klass.expressions.filter { ast -> ast.description == "classBody" }
        if (possibleClassBodies.isNotEmpty()) {
            val classBody = possibleClassBodies.first() as DefaultAstNode
            val classProperties = classBody.children
                .filterIsInstance(KlassDeclaration::class.java)
                .filter { declaration -> declaration.keyword == "var" || declaration.keyword == "val" }

            for (classProperty in classProperties) {
                val column = getTableColumnFromDeclaration(classProperty)

                if (column != null) {
                    columns.add(column)
                }
            }
        }

        return columns
    }

    private fun getTableColumnFromDeclaration(klassDecl: KlassDeclaration): Column? {
        val propertyIdentifier = klassDecl.identifier

        logger.info { "Determine column raw type" }
        var rawTypeName = ""
        if (klassDecl.type.isEmpty()) {
            logger.info { "Klass declaration type is empty" }
            if (klassDecl.children.size >= 2) {
                val valueNode = klassDecl.children[1] as DefaultAstNode
                if (valueNode.description != "literalConstant") {
                    TODO("Throw appropriate exception here")
                } else {
                    rawTypeName = valueNode.children.first().description
                }
            }
        } else {
            rawTypeName = klassDecl.type.first().identifier
            logger.info { "Klass declaration type is not empty $rawTypeName" }
        }

        var columnDataType = ColumnDataType.getTypeByVmString(rawTypeName)
        logger.info { "Type before assignment: ${columnDataType.rawTypeName}" }
        columnDataType.rawTypeName = rawTypeName
        logger.info { "Type after assignment: ${columnDataType.rawTypeName}" }

        if (propertyIdentifier != null) {

            var column: Column? = null
            var columnName: String?

            val annotations = klassDecl.annotations.map { getAnnotation(it) }

            for (parameterAnnotation in klassDecl.annotations) {
                val parsedAnnotation = getAnnotation(parameterAnnotation)
                val annotationName = parsedAnnotation.name
                val isId = annotationName == "Id"
                // handle better id and column annotations together on something
                if (annotationName == "Column" || isId) {
                    columnName = parsedAnnotation.parameters
                        .find { parameter -> parameter.name == "name" }
                        ?.stringVal
                        ?: getTableStyleName(propertyIdentifier.rawName)

                    val nullable = parsedAnnotation.parameters
                        .find { parameter -> parameter.name == "nullable" }
                        ?.booleanVal

                    val unique = parsedAnnotation.parameters
                        .find { parameter -> parameter.name == "unique" }
                        ?.booleanVal

                    val stringLength = parsedAnnotation.parameters
                        .find { parameter -> parameter.name == "length" }
                        ?.integerVal

                    val constraints = ColumnConstraint(nullable, isId, unique, stringLength)
                    if (column == null) {
                        column = Column(columnName, columnDataType, constraints, annotations)
                    } else {
                        TODO("Implement case when column is not the first column creating annotation")
                    }
                }

                if (annotationName == "JoinColumn") {
                    columnName = parsedAnnotation.parameters
                        .find { parameter -> parameter.name == "name" }
                        ?.stringVal
                        ?: getTableStyleName(propertyIdentifier.rawName)

                    if (column == null) {
                        column = Column(columnName, columnDataType, ColumnConstraint(true), annotations)
                    }
                }

                if (annotationName == "Enumerated") {
                    if (column == null) {

                        val enumeratedParameterDeclaration = parameterAnnotation.children.first() as KlassDeclaration
                        val enumeratedParameter = enumeratedParameterDeclaration.children.first() as KlassIdentifier
                        if (enumeratedParameter.identifier == "EnumType") {
                            val parameter = enumeratedParameter.children.first() as DefaultAstNode
                            val parameterSuffix = parameter.children.first() as DefaultAstNode
                            val enumType = parameterSuffix.children[1] as DefaultAstNode // ignoring the dot
                            val enumTypeValue = enumType.children.first() as DefaultAstTerminal
                            columnDataType = ColumnDataType.getTypeByVmString(enumTypeValue.text)
                            columnName = getTableStyleName(propertyIdentifier.rawName)
                            column = Column(columnName, columnDataType, ColumnConstraint(true), annotations)
                        } else {
                            TODO("Resolve parameter for enumerated annotation")
                        }
                    } else {
                        val enumeratedParameterDeclaration = parameterAnnotation.children.first() as KlassDeclaration
                        val enumeratedParameter = enumeratedParameterDeclaration.children.first() as KlassIdentifier
                        if (enumeratedParameter.identifier == "EnumType") {
                            val parameter = enumeratedParameter.children.first() as DefaultAstNode
                            val parameterSuffix = parameter.children.first() as DefaultAstNode
                            val enumType = parameterSuffix.children[1] as DefaultAstNode // ignoring the dot
                            val enumTypeValue = enumType.children.first() as DefaultAstTerminal
                            column.dataType = ColumnDataType.getTypeByVmString(enumTypeValue.text)
                        } else {
                            TODO("Resolve parameter for enumerated annotation")
                        }
                    }
                }
            }

            return column
        }

        return null
    }

    private fun getAnnotation(annotation: KlassAnnotation): Annotation {
        val annotationName = annotation.identifier.first().identifier
        val attributes = ArrayList<AnnotationParameter>()

        for (i in 0 until annotation.children.size) {
            val attributeValue = annotation.children[i] as KlassDeclaration
            val attribute = attributeValue.identifier

            if (attributeValue.children.size == 1) {
                var valueObj = attributeValue.children.first()
                if (valueObj.description == "literalConstant") {
                    valueObj = (valueObj as DefaultAstNode).children.first()
                }

                val parameterType = ParameterType.getTypeFromString(valueObj.description)
                val parameter = AnnotationParameter(attribute?.rawName ?: "", parameterType)
                attributes.add(parameter)

                when (parameterType) {
                    ParameterType.STRING -> parameter.stringVal =
                        ((valueObj as KlassString).children.first() as StringComponentRaw).string
                    ParameterType.INTEGER -> parameter.integerVal = (valueObj as DefaultAstTerminal).text.toInt()
                    ParameterType.BOOLEAN -> parameter.booleanVal = (valueObj as DefaultAstTerminal).text.toBoolean()
                    else -> ""
                }
            }
        }

        return Annotation(annotationName, attributes)
    }
}