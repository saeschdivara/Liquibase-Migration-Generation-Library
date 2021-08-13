package app.gitforge.libraries.liquibase.migration

import app.gitforge.libraries.liquibase.migration.parser.KotlinEntityParser
import app.gitforge.libraries.liquibase.migration.schema.Column
import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import app.gitforge.libraries.liquibase.migration.schema.Schema
import app.gitforge.libraries.liquibase.migration.schema.Table
import app.gitforge.libraries.liquibase.migration.yml.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


object MigrationGenerator {
    private val logger = KotlinLogging.logger {}

    private val mapper = let {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper
    }

    fun dumpMigration(oldStatePath: String, newStatePath: String, migrationFilePath: String) {
        val migrationResult = generateNewMigration(oldStatePath, newStatePath)

        if (migrationResult.databaseChangeLog.isNotEmpty()) {
            File(migrationFilePath).writeText(mapper.writeValueAsString(migrationResult))
        }
    }

    fun generateNewMigration(oldStatePath: String, newStatePath: String): ChangeLogEntryPoint = runBlocking {
        val oldSchemas = ArrayList<Schema>()

        val oldSchemaRun = launch {
            val schemaCalculator = SchemaChangeLogsCalculator(Schema())
            val migrationFiles = ArrayList<String>()

            File(oldStatePath).walk().forEach {
                if (it.isFile) {
                    migrationFiles.add(it.absolutePath)
                }
            }

            // migration files are not sorted correctly if only walking down the fs
            migrationFiles.sort()

            for (migrationFile in migrationFiles) {
                val logs: ChangeLogEntryPoint = mapper.readValue(Path.of(migrationFile))
                schemaCalculator.updateSchema(logs.databaseChangeLog)
            }

            oldSchemas.add(schemaCalculator.schema)
        }

        val tables = Collections.synchronizedList(ArrayList<Table>())

        runBlocking {
            File(newStatePath).walk().forEach {
                if (it.isFile && it.name.endsWith(".kt")) {
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

                    if (column.dataType == ColumnDataType.FOREIGN_KEY) {
                        val referencedTable = newSchema.getTableByClassName(column.dataType.rawTypeName)

                        changes.add(Change(addForeignKeyConstraint = AddForeignKeyConstraint(
                            baseTableName = table.name,
                            baseColumnNames = column.name,
                            constraintName = "${column.name}_fk",
                            referencedTableName = referencedTable!!.name,
                            referencedColumnNames = "id" // TODO: support reference column name extraction from annotations
                        )))
                    }
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
            val nullable = newColumn.constraints.nullable
            if (nullable != null && nullable) {
                // TODO: drop constraint
            } else {
                changes.add(Change(
                    addNotNullConstraint = AddNotNullConstraintChange(table.name, oldColumn.name, oldColumn.getColumnDataType())
                ))
            }
        }

        if (oldColumn.constraints.length != newColumn.constraints.length) {
            changes.add(Change(
                modifyDataType = ModifyDataTypeChange(
                    table.name,
                    newColumn.name,
                    oldColumn.getColumnDataType(),
                    newColumn.getColumnDataType()
                )
            ))
        }

        // check for datatype change
        if (oldColumn.dataType != newColumn.dataType) {
            if (newColumn.dataType == ColumnDataType.FOREIGN_KEY) {
                // migrate to foreign key
            } else {
                //
            }
        }

        return changes
    }

    private fun generateChangeSetId() : String {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    }

}