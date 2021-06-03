package app.gitforge.libraries.liquibase.migration.schema

data class Schema(val name: String? = null, val tables: MutableList<Table> = ArrayList()) {
    fun getTableByName(name: String) : Table? {
        return tables.find { it.name.toLowerCase() == name }
    }
}