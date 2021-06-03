package app.gitforge.libraries.liquibase.migration.yml

import app.gitforge.libraries.liquibase.migration.schema.Column as SchemaColumn

data class ColumnConstraint(val primaryKey: Boolean = false, val primaryKeyName: String?, val nullable: Boolean?, val unique: Boolean = false)
data class Column(val name: String, val type: String, val autoIncrement: Boolean?, val constraints: ColumnConstraint)
data class ChangeColumn(val column: Column) {
    companion object {
        fun fromSchema(column: SchemaColumn) : ChangeColumn {

            var columnConstraint = column.constraints
            val constraint = ColumnConstraint(
                columnConstraint.isPrimaryKey,
                null,
                columnConstraint.nullable,
                false
            )

            return ChangeColumn(Column(
                column.name,
                column.dataType.name,
                false,
                constraint
            ))
        }
    }
}
data class AddColumnChange(val tableName: String, val columns: List<ChangeColumn>)
data class CreateTableChange(val tableName: String, val columns: List<ChangeColumn>)
data class DropTableChange(val tableName: String, val cascadeConstraints: Boolean? = true)

data class Change(
    val createTable: CreateTableChange? = null,
    val dropTable: DropTableChange? = null,
    val addColumn: AddColumnChange? = null,
)

data class ChangeSet(val id: String, val author: String, val changes: List<Change>)
data class DatabaseChangeLog(val changeSet: ChangeSet)
data class ChangeLogEntryPoint(val databaseChangeLog: List<DatabaseChangeLog>)