package app.gitforge.libraries.liquibase.migration.schema

import java.util.*
import kotlin.collections.ArrayList

data class Schema(val name: String? = null, val tables: MutableList<Table> = ArrayList()) {
    fun getTableByName(name: String) : Table? {
        return tables.find { it.name.lowercase(Locale.getDefault()) == name }
    }

    fun getTableByClassName(name: String) : Table? {

        // handle case when generics are used (lists / sets)
        if (name.contains("<")) {
            val realClassName = name.split("<")[1].replace(">", "")
            return tables.find { it.clsName == realClassName }
        }

        return tables.find { it.clsName == name }
    }
}