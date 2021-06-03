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
    }

    @Test
    fun generateMigrationWhenFieldIsUpdated() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-003/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-003/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)
    }

    @Test
    fun generateMigrationWhenFieldIsRemoved() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-004/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-004/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)
    }

    @Test
    fun generateMigrationWhenTableIsAdded() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)
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