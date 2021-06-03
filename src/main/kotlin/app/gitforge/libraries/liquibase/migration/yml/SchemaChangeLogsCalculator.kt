package app.gitforge.libraries.liquibase.migration.yml

import app.gitforge.libraries.liquibase.migration.schema.Column
import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import app.gitforge.libraries.liquibase.migration.schema.Schema
import app.gitforge.libraries.liquibase.migration.schema.Table

class SchemaChangeLogsCalculator(val schema: Schema) {

    fun updateSchema(changeLogs: List<DatabaseChangeLog>) {
        for (changeLog in changeLogs) {
            val changeSet = changeLog.changeSet
            for (change in changeSet.changes) {
                if (change.createTable != null) {
                    val action = change.createTable
                    val table = Table(action.tableName, getColumns(action.columns))

                    schema.tables.add(table)
                }
            }
        }
    }

    private fun getColumns(columnDescriptions: List<ChangeColumn>): List<Column> {
        val columns = ArrayList<Column>()

        for (columnDescription in columnDescriptions) {
            val columnData = columnDescription.column
            val column = Column(
                columnData.name,
                ColumnDataType.getTypeByMigrationString(columnData.type),
                columnData.constraints.nullable ?: true
            )

            columns.add(column)
        }

        return columns
    }

}