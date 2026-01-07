package org.entur.ror.ashur.sax.plugins.filenames

class FileNamePluginContext{
    var lineType: String = ""
    var lineName: StringBuilder = StringBuilder()
    var linePublicCode: StringBuilder = StringBuilder()
    var linePrivateCode: StringBuilder = StringBuilder()

    fun reset() {
        lineType = ""
        lineName.clear()
        linePublicCode.clear()
        linePrivateCode.clear()
    }
}