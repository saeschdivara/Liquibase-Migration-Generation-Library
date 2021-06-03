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
    fun generateNoMigrationWhenFieldIsAdded() {
        val migration = MigrationGenerator.generateNewMigration(
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/old",
            "src/test/resources/test-data/MigrationGeneratorTest/dataset-002/new"
        )

        assertEquals(1, migration.databaseChangeLog.size)
    }
}