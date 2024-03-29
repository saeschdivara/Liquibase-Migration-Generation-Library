package app.gitforge.libraries.liquibase.migration.yml

import com.fasterxml.jackson.annotation.JsonInclude
import app.gitforge.libraries.liquibase.migration.schema.Column as SchemaColumn

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ColumnConstraint(val primaryKey: Boolean?, val primaryKeyName: String?, val nullable: Boolean?, val unique: Boolean?)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Column(val name: String, var type: String, val autoIncrement: Boolean?, val constraints: ColumnConstraint?, val defaultValue: Any?)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChangeColumn(val column: Column) {
    companion object {
        fun fromSchema(column: SchemaColumn) : ChangeColumn {
            val columnConstraint = column.constraints
            var isPrimaryKey: Boolean? = null
            if (columnConstraint.isPrimaryKey != null && columnConstraint.isPrimaryKey!!) {
                isPrimaryKey = true
            }

            val constraint = ColumnConstraint(
                isPrimaryKey,
                null,
                columnConstraint.nullable,
                columnConstraint.isUnique
            )

            return ChangeColumn(Column(
                column.name,
                column.getColumnDataType(),
                null,
                constraint,
                defaultValue = column.defaultValue
            ))
        }
    }
}
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddColumnChange(val tableName: String, val columns: List<ChangeColumn>)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DropColumnChange(val tableName: String, val columnName: String?, val columns: List<ChangeColumn> = ArrayList())
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddPrimaryKeyChange(val tableName: String, val columnNames: String, val constraintName: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddNotNullConstraintChange(val tableName: String, val columnName: String, val columnDataType: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddForeignKeyConstraint(
    val baseTableName: String, val baseColumnNames: String, val constraintName: String,
    val onDelete: String? = null, val onUpdate: String? = null,
    val referencedTableName: String, val referencedColumnNames: String,
    val validate: Boolean? = null
)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModifyDataTypeChange(val tableName: String, val columnName: String, val columnDataType: String, val newDataType: String)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateTableChange(val tableName: String, val columns: List<ChangeColumn>)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DropTableChange(val tableName: String, val cascadeConstraints: Boolean? = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Change(
    val createTable: CreateTableChange? = null,
    val dropTable: DropTableChange? = null,
    val addColumn: AddColumnChange? = null,
    val dropColumn: DropColumnChange? = null,
    val addPrimaryKey: AddPrimaryKeyChange? = null,
    val addNotNullConstraint: AddNotNullConstraintChange? = null,
    val addForeignKeyConstraint: AddForeignKeyConstraint? = null,
    val modifyDataType: ModifyDataTypeChange? = null,
)

data class ChangeSet(val id: String, val author: String, val changes: List<Change>)
data class DatabaseChangeLog(val changeSet: ChangeSet)
data class ChangeLogEntryPoint(val databaseChangeLog: List<DatabaseChangeLog>)