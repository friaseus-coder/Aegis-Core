package com.antigravity.aegis.domain.transfer

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvHelper {

    fun validateHeader(headerRow: List<String>, expectedColumns: List<String>): List<String> {
        val errors = mutableListOf<String>()
        val missing = expectedColumns.filter { !headerRow.contains(it) }
        if (missing.isNotEmpty()) {
            errors.add("Missing columns: ${missing.joinToString(", ")}")
        }
        return errors
    }

    fun parse(inputStream: InputStream): List<Map<String, String>> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return emptyList()

        val header = lines[0].split(",").map { it.trim() }
        val result = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            
            // Handle quotes later if needed, simple split for now
            val values = line.split(",").map { it.trim() }
            
            // Pad with empty strings if row is shorter than header
            val rowMap = mutableMapOf<String, String>()
            header.forEachIndexed { index, colName ->
                rowMap[colName] = if (index < values.size) values[index] else ""
            }
            result.add(rowMap)
        }
        return result
    }
}
