package me.aiglez.service.data.csv

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.csvReader
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.SchemaField

data class CsvImportSource(
    val fileName: String,
    val delimiter: Char,
    val headers: List<String>,
    val rows: List<CsvImportRow>,
)

data class CsvImportRow(
    val sourceRowNumber: Int,
    val values: Map<String, String>,
)

sealed interface CsvImportReadResult {
    data class Success(val source: CsvImportSource) : CsvImportReadResult
    data class Failure(val message: String) : CsvImportReadResult
}

enum class CsvImportIssueSeverity {
    Error,
    Warning,
}

data class CsvImportIssue(
    val severity: CsvImportIssueSeverity,
    val message: String,
    val rowNumber: Int? = null,
    val fieldName: String? = null,
    val columnName: String? = null,
)

data class CsvImportPreview(
    val records: List<Map<String, String>>,
    val issues: List<CsvImportIssue>,
) {
    val errors: List<CsvImportIssue> = issues.filter { it.severity == CsvImportIssueSeverity.Error }
    val warnings: List<CsvImportIssue> = issues.filter { it.severity == CsvImportIssueSeverity.Warning }
    val canImport: Boolean = errors.isEmpty() && records.isNotEmpty()
}

object DataRecordCsvImporter {
    private val delimiterCandidates = listOf(',', ';', '\t')

    fun read(fileName: String, content: String): CsvImportReadResult {
        val rawContent = content.trimStart('\uFEFF')
        if (rawContent.isBlank()) {
            return CsvImportReadResult.Failure("Le fichier CSV est vide.")
        }

        val source = delimiterCandidates
            .mapNotNull { delimiter -> parseWithDelimiter(fileName, rawContent, delimiter) }
            .maxByOrNull { it.headers.size }
            ?: return CsvImportReadResult.Failure(
                "Le fichier CSV ne peut pas être lu. Vérifiez son format et ses guillemets.",
            )

        validateHeaders(source.headers)?.let { return CsvImportReadResult.Failure(it) }
        if (source.rows.isEmpty()) {
            return CsvImportReadResult.Failure("Le fichier CSV ne contient aucune ligne de données.")
        }
        return CsvImportReadResult.Success(source)
    }

    fun suggestMappings(schema: DataSchema, source: CsvImportSource): Map<String, String?> {
        val headersByKey = buildMap {
            source.headers.forEach { header ->
                put(header.normalizedForMapping(), header)
                put(header.slugForMapping(), header)
            }
        }
        return schema.fields.associate { field ->
            field.id to (
                headersByKey[field.name.normalizedForMapping()]
                    ?: headersByKey[field.slug.normalizedForMapping()]
                    ?: headersByKey[field.slug.slugForMapping()]
                )
        }
    }

    fun preview(
        schema: DataSchema,
        source: CsvImportSource,
        mappings: Map<String, String?>,
    ): CsvImportPreview {
        val issues = mutableListOf<CsvImportIssue>()
        mappings.values.filterNotNull()
            .filterNot { it in source.headers }
            .distinct()
            .forEach { column ->
                issues += CsvImportIssue(
                    severity = CsvImportIssueSeverity.Error,
                    message = "La colonne '$column' n'existe pas dans le fichier CSV.",
                    columnName = column,
                )
            }

        schema.fields.filter { mappings[it.id] == null }.forEach { field ->
            issues += CsvImportIssue(
                severity = CsvImportIssueSeverity.Warning,
                message = "Le champ '${field.name}' ne sera pas importé.",
                fieldName = field.name,
            )
        }

        val hasMappingErrors = issues.any { it.severity == CsvImportIssueSeverity.Error }
        val records = if (hasMappingErrors) {
            emptyList()
        } else {
            buildRecords(schema, source, mappings, issues)
        }
        return CsvImportPreview(records = records, issues = issues)
    }

    private fun buildRecords(
        schema: DataSchema,
        source: CsvImportSource,
        mappings: Map<String, String?>,
        issues: MutableList<CsvImportIssue>,
    ): List<Map<String, String>> {
        return source.rows.mapNotNull { row ->
            val values = mutableMapOf<String, String>()
            var hasError = false
            schema.fields.forEach { field ->
                val column = mappings[field.id] ?: return@forEach
                val rawValue = row.values[column].orEmpty()
                when (val parsed = parseValue(field, rawValue)) {
                    is ParsedValue.Success -> values[field.slug] = parsed.value
                    is ParsedValue.Failure -> {
                        hasError = true
                        issues += CsvImportIssue(
                            severity = CsvImportIssueSeverity.Error,
                            message = parsed.message,
                            rowNumber = row.sourceRowNumber,
                            fieldName = field.name,
                            columnName = column,
                        )
                    }
                }
            }
            values.takeUnless { hasError }
        }
    }

    private fun parseValue(field: SchemaField, rawValue: String): ParsedValue {
        val value = rawValue.trim()
        if (value.isBlank()) return ParsedValue.Success("")
        return when (field.type) {
            FieldType.TEXT,
            FieldType.REFERENCE -> ParsedValue.Success(rawValue)
            FieldType.NUMBER -> if (value.toLongOrNull() != null) {
                ParsedValue.Success(value)
            } else {
                ParsedValue.Failure("La valeur '$rawValue' n'est pas un nombre entier valide.")
            }
            FieldType.DOUBLE -> value.replace(',', '.').toDoubleOrNull()?.let {
                ParsedValue.Success(value.replace(',', '.'))
            } ?: ParsedValue.Failure("La valeur '$rawValue' n'est pas un nombre décimal valide.")
            FieldType.LIST -> ParsedValue.Success(
                rawValue.split(';').joinToString("\n") { it.trim() }.trim(),
            )
        }
    }

    private fun parseWithDelimiter(fileName: String, content: String, delimiter: Char): CsvImportSource? {
        return runCatching {
            val parsedRows = csvReader {
                dialect = CsvDialect(delimiter = delimiter)
                skipEmptyLine = true
            }.readAll(content)
            val headers = parsedRows.firstOrNull()?.map { it.trim() }.orEmpty()
            val rows = parsedRows.drop(1).mapIndexed { index, row ->
                CsvImportRow(
                    sourceRowNumber = index + 2,
                    values = headers.mapIndexed { columnIndex, header ->
                        header to row.getOrNull(columnIndex).orEmpty()
                    }.toMap(),
                )
            }
            CsvImportSource(fileName, delimiter, headers, rows)
        }.getOrNull()
    }

    private fun validateHeaders(headers: List<String>): String? {
        if (headers.isEmpty()) return "Le CSV doit contenir une ligne d'en-tête."
        val blankIndex = headers.indexOfFirst { it.isBlank() }
        if (blankIndex >= 0) return "La colonne ${blankIndex + 1} a un en-tête vide."
        val duplicate = headers.groupBy { it.normalizedForMapping() }
            .values.firstOrNull { it.size > 1 }?.firstOrNull()
        return duplicate?.let { "L'en-tête '$it' est présent plusieurs fois dans le CSV." }
    }

    private sealed interface ParsedValue {
        data class Success(val value: String) : ParsedValue
        data class Failure(val message: String) : ParsedValue
    }

    private fun String.normalizedForMapping(): String = trim().lowercase()

    private fun String.slugForMapping(): String = trim().lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
}
