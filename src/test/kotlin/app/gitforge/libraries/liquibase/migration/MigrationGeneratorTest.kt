package app.gitforge.libraries.liquibase.migration

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MigrationGeneratorTest {
    private val logger = KotlinLogging.logger {}

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
        assertEquals("varchar(40)", modifyDataType.columnDataType)
        assertEquals("varchar(60)", modifyDataType.newDataType)
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

    @Test
    fun `test migration generation for foreign keys without changes`() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-007/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-007/new"
        )

        assertEquals(0, migration.databaseChangeLog.size)
    }

    @Test
    fun `test migration generation for missing foreign keys`() {
        logger.info { "Start test 'test migration generation for missing foreign keys'" }

        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-009/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-009/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        assertEquals(2, changeLog.changeSet.changes.size)

        val firstChange = changeLog.changeSet.changes.first()
        assertNotNull(firstChange.addColumn)
        val addColumnChange = firstChange.addColumn!!
        assertEquals(1, addColumnChange.columns.size)
        val addedColumn = addColumnChange.columns.first().column
        assertEquals("bigint", addedColumn.type)

        val secondChange = changeLog.changeSet.changes[1]
        assertNotNull(secondChange.addForeignKeyConstraint)
    }

    @Test
    fun `test migration generation for no old data`() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-008/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-008/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        val changes = changeLog.changeSet.changes

        assertEquals(1, changes.size)
        val change = changes.first()

        assertNotNull(change.createTable)
        val createTable = change.createTable!!

        assertEquals(8, createTable.columns.size)

        val providerColumn = createTable.columns[6].column
        assertEquals("provider", providerColumn.name)
        assertEquals("varchar", providerColumn.type)
    }

    @Test
    fun `test migration generation for constant data`() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-008/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-008/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)

        val changeLog = migration.databaseChangeLog.first()
        val changes = changeLog.changeSet.changes

        assertEquals(1, changes.size)
        val change = changes.first()

        assertNotNull(change.createTable)
        val createTable = change.createTable!!

        assertEquals(8, createTable.columns.size)

        val emailVerifiedColumn = createTable.columns[4].column
        assertEquals("email_verified", emailVerifiedColumn.name)
        assertEquals("bool", emailVerifiedColumn.type)
    }

    @Test
    fun `test migration generation for embedded key`() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-010/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-010/new"
        )

        assertEquals(4, migration.databaseChangeLog.size)
        val changeLog = migration.databaseChangeLog.find {
            val changes = it.changeSet.changes
            if (changes.size != 2) {
                false
            } else {
                val change = changes.first()
                change.createTable != null
            }
        }

        assertNotNull(changeLog)

        val primaryKeyChange = changeLog!!.changeSet.changes[1]
        assertNotNull(primaryKeyChange.addPrimaryKey)

        val addPrimaryKey = primaryKeyChange.addPrimaryKey!!
        val primaryKeyColumns = addPrimaryKey.columnNames.split(",")

        assertEquals(2, primaryKeyColumns.size)
        assertEquals("user_id", primaryKeyColumns[0])
        assertEquals("account_id", primaryKeyColumns[1])
    }
}