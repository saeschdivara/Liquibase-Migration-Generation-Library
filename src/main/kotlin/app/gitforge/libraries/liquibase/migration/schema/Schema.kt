package app.gitforge.libraries.liquibase.migration.schema

import java.util.*
import kotlin.collections.ArrayList

data class Schema(val name: String? = null, val tables: MutableList<Table> = ArrayList()) {
    fun getTableByName(name: String) : Table? {
        return tables.find { it.name.lowercase(Locale.getDefault()) == name }
    }

    fun getTableByClassName(name: String) : Table? {
        return tables.find { it.clsName == name }
    }
}