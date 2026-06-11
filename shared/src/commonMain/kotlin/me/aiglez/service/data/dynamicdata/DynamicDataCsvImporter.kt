package me.aiglez.service.data.dynamicdata

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.csvReader

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
    val instances: List<DynamicDataInstance>,
    val issues: List<CsvImportIssue>,
) {
    val errors: List<CsvImportIssue> = issues.filter { it.severity == CsvImportIssueSeverity.Error }
    val warnings: List<CsvImportIssue> = issues.filter { it.severity == CsvImportIssueSeverity.Warning }
    val canImport: Boolean = errors.isEmpty() && instances.isNotEmpty()
}

object DynamicDataCsvImporter {
    private val delimiterCandidates = listOf(',', ';', '\t')

    fun read(fileName: String, content: String): CsvImportReadResult {
        val rawContent = content.trimStart('\uFEFF')
        if (rawContent.isBlank()) {
            return CsvImportReadResult.Failure("Le fichier CSV est vide.")
        }

        val parsedCandidates = delimiterCandidates.mapNotNull { delimiter ->
            parseWithDelimiter(fileName = fileName, content = rawContent, delimiter = delimiter)
        }
        val bestCandidate = parsedCandidates.maxByOrNull { it.headers.size }
            ?: return CsvImportReadResult.Failure("Le fichier CSV ne peut pas être lu. Vérifiez le format et les guillemets.")

        val validationFailure = validateHeaders(bestCandidate.headers)
        if (validationFailure != null) {
            return CsvImportReadResult.Failure(validationFailure)
        }
        if (bestCandidate.rows.isEmpty()) {
            return CsvImportReadResult.Failure("Le fichier CSV ne contient aucune ligne de données.")
        }

        return CsvImportReadResult.Success(bestCandidate)
    }

    fun suggestMappings(
        dynamicData: DynamicData,
        source: CsvImportSource,
    ): Map<String, String?> {
        val headersByNormalizedName = source.headers.associateBy { it.normalizedForMapping() }
        return dynamicData.fields.associate { field ->
            field.name to headersByNormalizedName[field.name.normalizedForMapping()]
        }
    }

    fun preview(
        dynamicData: DynamicData,
        source: CsvImportSource,
        mappings: Map<String, String?>,
    ): CsvImportPreview {
        val issues = mutableListOf<CsvImportIssue>()
        val mappedColumns = mappings.values.filterNotNull()

        mappedColumns
            .filterNot { it in source.headers }
            .distinct()
            .forEach { column ->
                issues += CsvImportIssue(
                    severity = CsvImportIssueSeverity.Error,
                    message = "La colonne '$column' n'existe pas dans le CSV.",
                    columnName = column,
                )
            }

        dynamicData.fields.forEach { field ->
            val mappedColumn = mappings[field.name]
            if (mappedColumn == null) {
                val severity = if (field.optional) CsvImportIssueSeverity.Warning else CsvImportIssueSeverity.Error
                val message = if (field.optional) {
                    "Le champ optionnel '${field.name}' ne sera pas importé."
                } else {
                    "Le champ obligatoire '${field.name}' doit être associé à une colonne CSV."
                }
                issues += CsvImportIssue(
                    severity = severity,
                    message = message,
                    fieldName = field.name,
                )
            }
        }

        val hasMappingErrors = issues.any { it.severity == CsvImportIssueSeverity.Error && it.rowNumber == null }
        val instances = if (hasMappingErrors) {
            emptyList()
        } else {
            buildInstances(dynamicData = dynamicData, source = source, mappings = mappings, issues = issues)
        }

        return CsvImportPreview(instances = instances, issues = issues)
    }

    private fun parseWithDelimiter(
        fileName: String,
        content: String,
        delimiter: Char,
    ): CsvImportSource? {
        return runCatching {
            val rows = csvReader {
                dialect = CsvDialect(delimiter = delimiter)
                skipEmptyLine = true
            }.readAll(content)

            val headers = rows.firstOrNull()?.map { it.trim() }.orEmpty()
            val dataRows = rows.drop(1).mapIndexed { index, row ->
                CsvImportRow(
                    sourceRowNumber = index + 2,
                    values = headers.mapIndexed { headerIndex, header ->
                        header to row.getOrNull(headerIndex).orEmpty()
                    }.toMap(),
                )
            }
            CsvImportSource(
                fileName = fileName,
                delimiter = delimiter,
                headers = headers,
                rows = dataRows,
            )
        }.getOrNull()
    }

    private fun validateHeaders(headers: List<String>): String? {
        if (headers.isEmpty()) {
            return "Le CSV doit contenir une ligne d'en-tête."
        }
        val blankHeaderIndex = headers.indexOfFirst { it.isBlank() }
        if (blankHeaderIndex >= 0) {
            return "La colonne ${blankHeaderIndex + 1} a un en-tête vide."
        }

        val duplicateHeader = headers
            .groupBy { it.normalizedForMapping() }
            .firstNotNullOfOrNull { (_, matchingHeaders) ->
                matchingHeaders.firstOrNull().takeIf { matchingHeaders.size > 1 }
            }
        if (duplicateHeader != null) {
            return "L'en-tête '$duplicateHeader' est présent plusieurs fois dans le CSV."
        }

        return null
    }

    private fun buildInstances(
        dynamicData: DynamicData,
        source: CsvImportSource,
        mappings: Map<String, String?>,
        issues: MutableList<CsvImportIssue>,
    ): List<DynamicDataInstance> {
        return source.rows.mapNotNull { row ->
            val rowValues = mutableMapOf<String, DynamicDataValue?>()
            var rowHasError = false

            dynamicData.fields.forEach { field ->
                val columnName = mappings[field.name]
                if (columnName == null) {
                    rowValues[field.name] = null
                    return@forEach
                }

                val rawValue = row.values[columnName].orEmpty()
                when (val parsedValue = parseValue(field = field, rawValue = rawValue)) {
                    is ParsedDynamicDataValue.Success -> rowValues[field.name] = parsedValue.value
                    is ParsedDynamicDataValue.Failure -> {
                        rowHasError = true
                        issues += CsvImportIssue(
                            severity = CsvImportIssueSeverity.Error,
                            message = parsedValue.message,
                            rowNumber = row.sourceRowNumber,
                            fieldName = field.name,
                            columnName = columnName,
                        )
                    }
                }
            }

            if (rowHasError) {
                null
            } else {
                DynamicDataInstance(
                    id = 0L,
                    dynamicDataId = dynamicData.id,
                    values = rowValues,
                )
            }
        }
    }

    private fun parseValue(
        field: DynamicDataField,
        rawValue: String,
    ): ParsedDynamicDataValue {
        return parseValue(
            fieldName = field.name,
            fieldType = field.type,
            optional = field.optional,
            rawValue = rawValue,
        )
    }

    private fun parseValue(
        fieldName: String,
        fieldType: DynamicDataFieldType,
        optional: Boolean,
        rawValue: String,
    ): ParsedDynamicDataValue {
        val value = rawValue.trim()
        if (value.isBlank()) {
            return if (optional) {
                ParsedDynamicDataValue.Success(null)
            } else {
                ParsedDynamicDataValue.Failure("Le champ obligatoire '$fieldName' est vide.")
            }
        }

        return when (fieldType) {
            DynamicDataFieldType.Text -> ParsedDynamicDataValue.Success(DynamicDataValue.Text(rawValue))
            DynamicDataFieldType.Number -> value.toLongOrNull()
                ?.let { ParsedDynamicDataValue.Success(DynamicDataValue.Number(it)) }
                ?: ParsedDynamicDataValue.Failure("La valeur '$rawValue' n'est pas un nombre entier valide.")
            DynamicDataFieldType.Decimal -> value.normalizedDecimal().toDoubleOrNull()
                ?.let { ParsedDynamicDataValue.Success(DynamicDataValue.Decimal(it)) }
                ?: ParsedDynamicDataValue.Failure("La valeur '$rawValue' n'est pas un nombre décimal valide.")
            DynamicDataFieldType.Boolean -> value.toBooleanImportValue()
                ?.let { ParsedDynamicDataValue.Success(DynamicDataValue.Boolean(it)) }
                ?: ParsedDynamicDataValue.Failure("La valeur '$rawValue' n'est pas un booléen valide (oui/non, true/false, 1/0).")
            is DynamicDataFieldType.DynamicDataRef -> value.toLongOrNull()
                ?.let { ParsedDynamicDataValue.Success(DynamicDataValue.DynamicDataRef(it)) }
                ?: ParsedDynamicDataValue.Failure("La référence '$rawValue' doit être un identifiant numérique.")
            is DynamicDataFieldType.ListOf -> {
                val values = value.split(';').mapIndexed { index, item ->
                    val itemFieldName = "$fieldName[${index + 1}]"
                    parseValue(
                        fieldName = itemFieldName,
                        fieldType = fieldType.itemType,
                        optional = false,
                        rawValue = item,
                    )
                }
                val firstFailure = values.firstOrNull { it is ParsedDynamicDataValue.Failure }
                    as? ParsedDynamicDataValue.Failure
                if (firstFailure != null) {
                    firstFailure
                } else {
                    ParsedDynamicDataValue.Success(
                        DynamicDataValue.ListOf(
                            values = values.map { (it as ParsedDynamicDataValue.Success).value }
                        )
                    )
                }
            }
        }
    }

    private sealed interface ParsedDynamicDataValue {
        data class Success(val value: DynamicDataValue?) : ParsedDynamicDataValue
        data class Failure(val message: String) : ParsedDynamicDataValue
    }

    private fun String.normalizedForMapping(): String {
        return trim().lowercase()
    }

    private fun String.normalizedDecimal(): String {
        return replace(',', '.')
    }

    private fun String.toBooleanImportValue(): Boolean? {
        return when (lowercase()) {
            "true", "t", "yes", "y", "oui", "o", "1" -> true
            "false", "f", "no", "n", "non", "0" -> false
            else -> null
        }
    }
}
