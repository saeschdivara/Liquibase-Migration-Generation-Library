package app.gitforge.libraries.liquibase.migration

import app.gitforge.libraries.liquibase.migration.kotlin.KotlinEntityParser
import app.gitforge.libraries.liquibase.migration.schema.Column
import app.gitforge.libraries.liquibase.migration.schema.Schema
import app.gitforge.libraries.liquibase.migration.schema.Table
import app.gitforge.libraries.liquibase.migration.yml.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


object MigrationGenerator {
    private val mapper = let {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper
    }

    fun generateNewMigration(oldStatePath: String, newStatePath: String): ChangeLogEntryPoint = runBlocking {
        val oldSchemas = ArrayList<Schema>()

        val oldSchemaRun = launch {
            val schemaCalculator = SchemaChangeLogsCalculator(Schema())

            File(oldStatePath).walk().forEach {
                if (it.isFile) {
                    val logs: ChangeLogEntryPoint = mapper.readValue(Path.of(it.absolutePath))
                    schemaCalculator.updateSchema(logs.databaseChangeLog)
                }
            }

            oldSchemas.add(schemaCalculator.schema)
        }

        val tables = Collections.synchronizedList(ArrayList<Table>())

        runBlocking {
            File(newStatePath).walk().forEach {
                if (it.isFile) {
                    launch {
                        val table = KotlinEntityParser.getTableFromEntityClass(it.absolutePath)
                        if (table != null) {
                            tables.add(table)
                        }
                    }
                }
            }
        }

        oldSchemaRun.join()

        val newSchema = Schema(null, tables)

        return@runBlocking calculateMigrations(oldSchemas.first(), newSchema)
    }

    private fun calculateMigrations(oldSchema: Schema, newSchema: Schema) : ChangeLogEntryPoint {
        val changeLogs = ArrayList<DatabaseChangeLog>()

        oldSchema.tables.forEach {
            val changeLog = calculateMigrationForTable(it, newSchema)

            if (changeLog != null) {
                changeLogs.add(changeLog)
            }
        }

        newSchema.tables.forEach {
            val tableName = it.name
            val tableInOldSchema = oldSchema.getTableByName(it.name)

            // new table found
            if (tableInOldSchema == null) {
                val changes = ArrayList<Change>()
                val columns = ArrayList<ChangeColumn>()

                for (column in it.columns) {
                    columns.add(ChangeColumn.fromSchema(column))
                }

                changes.add(Change(createTable = CreateTableChange(tableName, columns)))

                changeLogs.add(DatabaseChangeLog(
                    ChangeSet(generateChangeSetId(), "auto-gen-lib", changes)
                ))
            }
        }

        return ChangeLogEntryPoint(changeLogs)
    }

    private fun calculateMigrationForTable(table: Table, newSchema: Schema) : DatabaseChangeLog? {

        val tableInNewSchema = newSchema.getTableByName(table.name)

        if (tableInNewSchema == null) {
            // table was removed -> drop
            val changes = ArrayList<Change>()
            changes.add(Change(dropTable = DropTableChange(table.name)))

            return DatabaseChangeLog(
                ChangeSet(generateChangeSetId(), "auto-gen-lib", changes)
            )
        } else {
            val changes = ArrayList<Change>()

            // check for drops and updates
            for (column in table.columns) {
                val existingColumn = tableInNewSchema.getColumnByName(column.name)

                if (existingColumn == null) {
                    // column was removed
                    changes.add(Change(dropColumn = DropColumnChange(table.name, column.name)))
                } else {
                    val columnChanges = calculateMigrationForColumn(table, column, existingColumn)
                    changes.addAll(columnChanges)
                }
            }

            // check for new columns
            for (column in tableInNewSchema.columns) {
                val existingColumn = table.getColumnByName(column.name)

                if (existingColumn == null) {
                    // column was added
                    val columnChanges = ArrayList<ChangeColumn>()
                    columnChanges.add(ChangeColumn.fromSchema(column))

                    changes.add(Change(addColumn = AddColumnChange(table.name, columnChanges)))
                }
            }

            if (changes.size > 0) {
                return DatabaseChangeLog(
                    ChangeSet(generateChangeSetId(), "auto-gen-lib", changes)
                )
            }
        }

        return null
    }

    private fun calculateMigrationForColumn(table: Table, oldColumn: Column, newColumn: Column) : List<Change> {

        val changes = ArrayList<Change>()

        if (oldColumn == newColumn) {
            return emptyList()
        }

        if (oldColumn.constraints.nullable != newColumn.constraints.nullable) {
            if (newColumn.constraints.nullable) {
                // TODO: drop constraint
            } else {
                changes.add(Change(
                    addNotNullConstraint = AddNotNullConstraintChange(table.name, oldColumn.name, oldColumn.getColumnDataType())
                ))
            }
        }

        if (oldColumn.constraints.lenght != newColumn.constraints.lenght) {
            changes.add(Change(
                modifyDataType = ModifyDataTypeChange(table.name, newColumn.name, newColumn.getColumnDataType())
            ))
        }

        return changes
    }

    private fun generateChangeSetId() : String {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    }

}