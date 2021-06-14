package app.gitforge.libraries.liquibase.migration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MigrationGeneratorTest {

    @Test
    fun generateNoMigrationWhenNothingHasChanged() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-001/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-001/new"
        )

        assertEquals(0, migration.databaseChangeLog.size)
    }

    @Test
    fun generateMigrationWhenFieldIsAdded() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        assertEquals(1, changeLog.changeSet.changes.size)

        val change = changeLog.changeSet.changes.first()
        assertNotNull(change.addColumn)

        val addColumnChange = change.addColumn!!
        assertEquals("bank", addColumnChange.tableName)

        assertEquals(1, addColumnChange.columns.size)
        val changeColumn = addColumnChange.columns.first().column

        assertEquals("description", changeColumn.name)
        assertEquals("varchar(40)", changeColumn.type)

        assertEquals(false, changeColumn.constraints!!.nullable)
        assertEquals(true, changeColumn.constraints!!.unique)
    }

    @Test
    fun `generate migration when string field length has changed`() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-003/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-003/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        assertEquals(1, changeLog.changeSet.changes.size)

        val change = changeLog.changeSet.changes.first()
        assertNotNull(change.modifyDataType)

        val modifyDataType = change.modifyDataType!!
        assertEquals("bank", modifyDataType.tableName)
        assertEquals("name", modifyDataType.columnName)
        assertEquals("varchar(60)", modifyDataType.columnDataType)
    }

    @Test
    fun generateMigrationWhenFieldIsRemoved() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-004/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-004/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        assertEquals(1, changeLog.changeSet.changes.size)

        val change = changeLog.changeSet.changes.first()
        assertNotNull(change.dropColumn)

        val dropColumn = change.dropColumn!!
        assertEquals("bank", dropColumn.tableName)
        assertEquals("name", dropColumn.columnName)
    }

    @Test
    fun generateMigrationWhenTableIsAdded() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-005/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-005/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        assertEquals(1, changeLog.changeSet.changes.size)

        val change = changeLog.changeSet.changes.first()
        assertNotNull(change.createTable)

        val createTable = change.createTable!!
        assertEquals("bank_account", createTable.tableName)
        assertEquals(2, createTable.columns.size)

        val nameColumn = createTable.columns[0].column
        assertEquals("name", nameColumn.name)
        assertEquals("varchar(40)", nameColumn.type)

        val idColumn = createTable.columns[1].column
        assertEquals("id", idColumn.name)
        assertEquals("bigint", idColumn.type)
    }

    @Test
    fun generateMigrationWhenTableIsRemoved() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)
    }
}