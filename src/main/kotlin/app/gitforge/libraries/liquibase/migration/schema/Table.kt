package app.gitforge.libraries.liquibase.migration.schema

data class Table(val name: String, val columns: List<Column>) {
    fun getColumnByName(columnName: String) : Column? {
        return columns.find { it.name == columnName }
    }
}