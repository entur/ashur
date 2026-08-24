package org.entur.ror.ashur.sax.plugins.filenames

class FileNameBuilder {
    var codespace: String = ""
    var lineId: String = ""
    var lineType: String = ""
    var lineName: String = ""
    var linePublicCode: String = ""
    var linePrivateCode: String = ""

    fun withLineId(id: String): FileNameBuilder {
        this.lineId = sanitize(id)
        return this
    }

    fun withCodespace(codespace: String): FileNameBuilder {
        this.codespace = sanitize(codespace)
        return this
    }

    fun withLineType(lineType: String): FileNameBuilder {
        this.lineType = sanitize(lineType)
        return this
    }

    fun withLineName(lineName: String): FileNameBuilder {
        this.lineName = sanitize(lineName)
        return this
    }

    fun withLinePublicCode(linePublicCode: String): FileNameBuilder {
        // Trimmed so that a whitespace-only PublicCode counts as absent in build()
        this.linePublicCode = sanitize(linePublicCode, trimWhitespace = true)
        return this
    }

    fun withLinePrivateCode(linePrivateCode: String): FileNameBuilder {
        this.linePrivateCode = sanitize(linePrivateCode)
        return this
    }

    fun firstCode(): String = sanitize(linePrivateCode.ifEmpty { linePublicCode })

    private fun sanitize(fileNameString: String, trimWhitespace: Boolean = false): String {
        val input = if (trimWhitespace) fileNameString.trim() else fileNameString
        val transliterated = buildString(input.length) {
            for (ch in input) {
                val mapped = TRANSLITERATION[ch]
                if (mapped != null) append(mapped) else append(ch)
            }
        }
        return transliterated
            .replace(NON_ASCII, "")
            .replace(UNSAFE, "-")
    }

    fun build(): String {
        if (linePublicCode.isEmpty()) {
            return "${codespace.uppercase()}_${lineId}_${lineName}.xml"
        }
        return "${codespace.uppercase()}_${codespace.uppercase()}-${lineType}-${firstCode()}_${linePublicCode}_${lineName}.xml"
    }

    companion object {
        private val TRANSLITERATION: Map<Char, String> = mapOf(
            'Å' to "A", 'Ä' to "A", 'Â' to "A",
            'å' to "a", 'ä' to "a", 'á' to "a",
            'Ö' to "O", 'Ó' to "O", 'Ø' to "O",
            'ö' to "o", 'ø' to "o",
            'É' to "E", 'Ê' to "E", 'È' to "E", 'Æ' to "E",
            'é' to "e", 'è' to "e", 'ë' to "e", 'æ' to "e",
            'Ü' to "U", 'ü' to "u",
            'Ç' to "C",
            'ß' to "ss",
            'ª' to "", 'º' to "",
        )
        private val NON_ASCII = Regex("[^\\x00-\\x7F]")
        private val UNSAFE = Regex("""['./\\:<>"|?*;\s]""")
    }
}