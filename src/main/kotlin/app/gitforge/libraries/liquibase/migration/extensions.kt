package app.gitforge.libraries.liquibase.migration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path

// Extension function taking in Path as a parameter for ease of use

inline fun <reified T> ObjectMapper.readValue(src: Path): T = readValue(src.toFile())