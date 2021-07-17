package app.gitforge.libraries.liquibase.migration.parser

import app.gitforge.libraries.liquibase.migration.schema.ColumnDataType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class JavaEntityParserTest {

    private val basePath = "src/test/resources/test-data/JavaEntityParserTest"

    @Test
    fun `test parsing simple Java class`() {
        val entity = JavaEntityParser.getTableFromEntityClass("$basePath/Sample.java")

        assertNotNull(entity)

        assertEquals("sample", entity!!.name)
        assertEquals(4, entity.columns.size)

        val column1 = entity.columns[0]
        assertEquals("id", column1.name)
        assertEquals(ColumnDataType.LONG, column1.dataType)
        assertEquals(true, column1.constraints.isPrimaryKey)

        val column2 = entity.columns[1]
        assertEquals("bank", column2.name)
        assertEquals(ColumnDataType.FOREIGN_KEY, column2.dataType)
        assertEquals(false, column2.constraints.isPrimaryKey)
        assertEquals(true, column2.constraints.nullable)

        val column3 = entity.columns[2]
        assertEquals("sample_name", column3.name)
        assertEquals(ColumnDataType.STRING, column3.dataType)
        assertEquals(false, column3.constraints.isPrimaryKey)
        assertEquals(false, column3.constraints.nullable)
        assertEquals(false, column3.constraints.isUnique)
        assertEquals(40, column3.constraints.length)

        val column4 = entity.columns[3]
        assertEquals("account_number", column4.name)
        assertEquals(ColumnDataType.STRING, column4.dataType)
        assertEquals(false, column4.constraints.isPrimaryKey)
        assertEquals(true, column4.constraints.nullable)
        assertEquals(false, column4.constraints.isUnique)
        assertEquals(15, column4.constraints.length)
    }
}