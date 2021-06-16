package app.gitforge.libraries.liquibase.migration.yml

import app.gitforge.libraries.liquibase.migration.schema.Column
import app.gitforge.libraries.liquibase.migration.schema.ColumnConstraint
import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import app.gitforge.libraries.liquibase.migration.schema.Schema
import app.gitforge.libraries.liquibase.migration.schema.Table

class SchemaChangeLogsCalculator(val schema: Schema) {

    fun updateSchema(changeLogs: List<DatabaseChangeLog>) {
        for (changeLog in changeLogs) {
            val changeSet = changeLog.changeSet
            for (change in changeSet.changes) {
                println(change)
                if (change.createTable != null) {
                    val action = change.createTable
                    val table = Table(action.tableName, getColumns(action.columns))

                    schema.tables.add(table)
                }

                if (change.dropColumn != null) {
                    val action = change.dropColumn
                    val affectedTable = schema.getTableByName(action.tableName) ?: continue

                    val columnName = action.columnName
                    if (columnName != null) {
                        affectedTable.removeColumnByName(columnName)
                    } else {
                        for (column in action.columns) {
                            affectedTable.removeColumnByName(column.column.name)
                        }
                    }
                }
            }
        }
    }

    private fun getColumns(columnDescriptions: List<ChangeColumn>): MutableList<Column> {
        val columns = ArrayList<Column>()

        for (columnDescription in columnDescriptions) {
            val columnData = columnDescription.column
            val column = Column(
                columnData.name,
                ColumnDataType.getTypeByMigrationString(columnData.type),
                ColumnConstraint.fromYaml(columnData.constraints)
            )

            if (columnData.type.contains("(")) {
                val typeLengthStr = "\\((.*)\\)".toRegex().find(columnData.type)!!

                column.constraints.lenght = typeLengthStr.groupValues[1].toInt()
            }

            columns.add(column)
        }

        return columns
    }

}