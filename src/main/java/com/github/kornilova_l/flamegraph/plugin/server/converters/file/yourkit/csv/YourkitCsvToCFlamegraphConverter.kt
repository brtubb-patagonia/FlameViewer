package com.github.kornilova_l.flamegraph.plugin.server.converters.file.yourkit.csv

import com.github.kornilova_l.flamegraph.plugin.server.converters.file.CFlamegraph
import com.github.kornilova_l.flamegraph.plugin.server.converters.file.ProfilerToCompressedFlamegraphConverter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.PathUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


class YourkitCsvToCFlamegraphConverter : ProfilerToCompressedFlamegraphConverter {
    override fun isSupported(file: File): Boolean {
        if (!StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.name), "csv")) {
            return false
        }
        BufferedReader(FileReader(file)).use { reader ->

            reader.readLine() // skip header
            reader.readLine() // skip header

            var line: String? = reader.readLine()

            while (line != null) {
                val parts = line.split("\",\"")
                if (parts.size != 3) {
                    return false
                }
                val firstString = parts[0].removePrefix("\"")
                if (!isMethod(firstString) && !firstString.contains(':') && !firstString.startsWith('[')) {
                    return false
                }
                try {
                    Integer.parseInt(parts[1]) // time
                    Integer.parseInt(parts[2].removeSuffix("\"")) // level
                } catch (e: NumberFormatException) {
                    return false
                }

                line = reader.readLine()
            }
        }
        return true
    }

    private fun isMethod(s: String): Boolean {
        val openBracketPos = s.indexOf('(')
        val closeBracketPos = s.indexOf(')')
        return openBracketPos != -1 &&
                closeBracketPos != -1 &&
                openBracketPos < closeBracketPos
    }

    override fun convert(file: File): CFlamegraph = Converter(file).cFlamegraph
}