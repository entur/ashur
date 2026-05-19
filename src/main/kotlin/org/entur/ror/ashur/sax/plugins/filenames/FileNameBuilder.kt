package org.entur.ror.ashur.sax.plugins.filenames

class FileNameBuilder {
    var codespace: String = ""
    var lineType: String = ""
    var lineName: String = ""
    var linePublicCode: String = ""
    var linePrivateCode: String = ""

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
        this.linePublicCode = sanitize(linePublicCode)
        return this
    }

    fun withLinePrivateCode(linePrivateCode: String): FileNameBuilder {
        this.linePrivateCode = sanitize(linePrivateCode)
        return this
    }

    private fun sanitize(fileNameString: String): String {
        val transliterated = buildString(fileNameString.length) {
            for (ch in fileNameString) {
                val mapped = TRANSLITERATION[ch]
                if (mapped != null) append(mapped) else append(ch)
            }
        }
        return transliterated
            .replace(NON_ASCII, "")
            .replace(UNSAFE, "-")
    }

    fun build(): String {
        return "${codespace.uppercase()}_${codespace.uppercase()}-${lineType}-${linePrivateCode}_${linePublicCode}_${lineName}.xml"
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