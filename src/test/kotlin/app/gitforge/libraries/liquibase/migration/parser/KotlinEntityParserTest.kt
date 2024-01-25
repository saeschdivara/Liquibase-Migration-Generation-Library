package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KotlinEntityParserTest {

    private val basePath = "src/test/resources/test-data/KotlinEntityParserTest"

    @Test
    fun `Test parsing embedded class`() {
        val parsingResult = KotlinEntityParser.parse("$basePath/UserAccountRelationship.kt")
        val embeddedKeys = parsingResult.embeddedKeys

        assertEquals(1, embeddedKeys.size)
        val key = embeddedKeys.first()

        assertEquals("UserAccountRelationshipKey", key.className)
        assertEquals(2, key.columns.size)
    }

    @Test
    fun `Test parsing entity with only primary key`() {
        val parsingResult = KotlinEntityParser.parse("$basePath/SimpleBank.kt")
        val tables = parsingResult.tables

        assertEquals(1, tables.size)

        val realEntity = tables.first()
        assertEquals("simple_bank", realEntity.name)
        assertEquals(1, realEntity.columns.size)

        val primaryKey = realEntity.columns.first()
        assertEquals("id", primaryKey.name)
        assertEquals(ColumnDataType.LONG, primaryKey.dataType)
        assertEquals(null, primaryKey.constraints.nullable)
        assertEquals(true, primaryKey.constraints.isPrimaryKey)
    }

    @Test
    fun `Test parsing entity with a column with a default value`() {
        val parsingResult = KotlinEntityParser.parse("$basePath/SimpleBank3.kt")
        val tables = parsingResult.tables

        assertEquals(1, tables.size)

        val realEntity = tables.first()
        assertEquals("simple_bank3", realEntity.name)
        assertEquals(2, realEntity.columns.size)

        val primaryKey = realEntity.columns.first()
        assertEquals("total", primaryKey.name)
        assertEquals(ColumnDataType.LONG, primaryKey.dataType)
        assertEquals(false, primaryKey.constraints.nullable)
        assertEquals(false, primaryKey.constraints.isPrimaryKey)

        assertNotNull(primaryKey.defaultValue)
        assertEquals("10", primaryKey.defaultValue)
    }

    @Test
    fun `Test parsing entity with unique text field`() {
        val parsingResult = KotlinEntityParser.parse("$basePath/SimpleBank2.kt")
        val tables = parsingResult.tables

        assertEquals(1, tables.size)

        val realEntity = tables.first()
        assertEquals("simple_bank2", realEntity.name)
        assertEquals(2, realEntity.columns.size)

        val primaryKey = realEntity.columns.first()
        assertEquals("name", primaryKey.name)
        assertEquals(ColumnDataType.STRING, primaryKey.dataType)
        assertEquals(false, primaryKey.constraints.nullable)
        assertEquals(true, primaryKey.constraints.isUnique)
        assertEquals(false, primaryKey.constraints.isPrimaryKey)
        assertEquals(40, primaryKey.constraints.length)
    }

    @Test
    fun `Test parsing entity with foreign key`() {
        val parsingResult = KotlinEntityParser.parse("$basePath/BankAccount.kt")
        val tables = parsingResult.tables

        assertEquals(1, tables.size)

        val realEntity = tables.first()
        assertEquals("bank_account", realEntity.name)
        assertEquals(2, realEntity.columns.size)

        val foreignKey = realEntity.columns.first()
        assertEquals("bank_id", foreignKey.name)
        assertEquals(ColumnDataType.FOREIGN_KEY, foreignKey.dataType)
        assertEquals(true, foreignKey.constraints.nullable)
    }
}