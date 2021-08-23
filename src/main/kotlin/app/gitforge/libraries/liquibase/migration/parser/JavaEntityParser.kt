package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.*
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.expr.AnnotationExpr
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.utils.SourceRoot
import java.nio.file.Path

object JavaEntityParser : EntityParser {
    override fun getEmbeddedKeys(filePath: String): List<EmbeddedKey> {
        TODO("Not yet implemented")
    }

    override fun getTableFromEntityClass(filePath: String): Table? {
        val splittedPath = filePath.split("/")
        val sourcePath = splittedPath.subList(0, splittedPath.size - 1)
        val sources = SourceRoot(Path.of(sourcePath.joinToString("/")))
        val cu = sources.parse("", splittedPath[splittedPath.size - 1])

        var table: Table? = null

        cu.types.forEach { klass ->
            val entityAnnotation = klass.annotations.find { annotationExpr ->
                annotationExpr.name.asString() == "Entity"
            }

            if (entityAnnotation != null) {
                val tableName = getTableName(klass, entityAnnotation)
                val columns = getTableColumns(klass)
                table = Table(tableName, klass.nameAsString, columns)
            }
        }

        return table
    }

    private fun getTableName(klass: TypeDeclaration<*>, entityAnnotation: AnnotationExpr): String {
        val nameParameter = getAnnotationParameter(entityAnnotation, "name")

        if (nameParameter == null) {
            return getTableStyleName(klass.nameAsString)
        }

        return nameParameter.value.toString()
    }

    private fun getAnnotationParameter(annotationExpr: AnnotationExpr, key: String): MemberValuePair? {
        val childNode = annotationExpr.childNodes.find {
            if (it is MemberValuePair) {
                return@find it.nameAsString == key
            }

            return@find false
        }

        return childNode as MemberValuePair?
    }

    private fun getTableColumns(klass: TypeDeclaration<*>): MutableList<Column> {
        val fields = klass.members
            .filterIsInstance<FieldDeclaration>()
            .filter {
                return@filter it.annotations.find { annotationExpr ->
                    when (annotationExpr.nameAsString) {
                        "Column" -> true
                        "Id" -> true
                        "OneToMany" -> true
                        "ManyToOne" -> true
                        "ManyToMany" -> true
                        else -> false
                    }
                } != null
            }

        return fields.map {
            val typeName = (it.variables[0].type as ClassOrInterfaceType).nameAsString
            val columnConstraint: ColumnConstraint
            val annotations = it.annotations.map {
                getAnnotation(it)
            }

            val idAnnotation = it.getAnnotationByName("Id")
            val isIdColumn = idAnnotation.isPresent

            val columnAnnotation = it.getAnnotationByName("Column")
            if (columnAnnotation.isPresent) {
                var nullable = true
                var unique = false
                var length = 0

                columnAnnotation.get().childNodes.filterIsInstance(MemberValuePair::class.java).forEach {
                    when(it.nameAsString) {
                        "nullable" -> nullable = it.value.asBooleanLiteralExpr().value
                        "unique" -> unique = it.value.asBooleanLiteralExpr().value
                        "length" -> length = it.value.asIntegerLiteralExpr().asNumber().toInt()
                    }
                }
                columnConstraint = ColumnConstraint(nullable, isPrimaryKey = isIdColumn, isUnique = unique, length = length)
            } else {
                columnConstraint = ColumnConstraint(!isIdColumn, isPrimaryKey = isIdColumn)
            }

            Column(
                getColumnName(it),
                ColumnDataType.getTypeByVmString(typeName),
                columnConstraint,
                annotations
            )
        } as MutableList<Column>
    }

    private fun getColumnName(field: FieldDeclaration): String {
        val fieldName = getTableStyleName(field.variables[0].nameAsString)
        val columnAnnotation = field.getAnnotationByName("Column")

        if (columnAnnotation.isEmpty) {
            return fieldName
        }

        val namePair = getAnnotationParameter(columnAnnotation.get(), "name") ?: return fieldName

        return namePair.value.asStringLiteralExpr().asString()
    }

    private fun getAnnotation(expr: AnnotationExpr): Annotation {
        return Annotation(expr.nameAsString, ArrayList())
    }
}