package app.gitforge.libraries.liquibase.migration.kotlin

import app.gitforge.libraries.liquibase.migration.parser.KotlinEntityParser
import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KotlinEntityParserTest {

    private val basePath = "src/test/resources/test-data/KotlinEntityParserTest"

    @Test
    fun `Test parsing entity with only primary key`() {
        val entity = KotlinEntityParser.getTableFromEntityClass("$basePath/SimpleBank.kt")

        assertNotNull(entity)

        val realEntity = entity!!
        assertEquals("simple_bank", realEntity.name)
        assertEquals(1, realEntity.columns.size)

        val primaryKey = realEntity.columns.first()
        assertEquals("id", primaryKey.name)
        assertEquals(ColumnDataType.LONG, primaryKey.dataType)
        assertEquals(false, primaryKey.constraints.nullable)
        assertEquals(true, primaryKey.constraints.isPrimaryKey)
    }

    @Test
    fun `Test parsing entity with unique text field`() {
        val entity = KotlinEntityParser.getTableFromEntityClass("$basePath/SimpleBank2.kt")

        assertNotNull(entity)

        val realEntity = entity!!
        assertEquals("simple_bank2", realEntity.name)
        assertEquals(2, realEntity.columns.size)

        val primaryKey = realEntity.columns.first()
        assertEquals("name", primaryKey.name)
        assertEquals(ColumnDataType.STRING, primaryKey.dataType)
        assertEquals(false, primaryKey.constraints.nullable)
        assertEquals(true, primaryKey.constraints.isUnique)
        assertEquals(false, primaryKey.constraints.isPrimaryKey)
        assertEquals(40, primaryKey.constraints.lenght)
    }

    @Test
    fun `Test parsing entity with foreign key`() {
        val entity = KotlinEntityParser.getTableFromEntityClass("$basePath/BankAccount.kt")

        assertNotNull(entity)

        val realEntity = entity!!
        assertEquals("bank_account", realEntity.name)
        assertEquals(2, realEntity.columns.size)

        val foreignKey = realEntity.columns.first()
        assertEquals("bank_id", foreignKey.name)
        assertEquals(ColumnDataType.FOREIGN_KEY, foreignKey.dataType)
        assertEquals(true, foreignKey.constraints.nullable)
    }
}