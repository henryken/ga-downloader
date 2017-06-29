package io.suryawirawan.henry.ga.service

import com.google.api.services.analytics.Analytics
import com.google.api.services.analytics.model.GaData
import io.suryawirawan.henry.ga.model.Properties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import java.io.FileWriter

@Service
class GoogleAnalyticsDownloaderImpl : GoogleAnalyticsDownloader {

    private val logger = LoggerFactory.getLogger(GoogleAnalyticsDownloaderImpl::class.java)

    @Autowired
    lateinit var analytics: Analytics

    @Autowired
    lateinit var properties: Properties

    override fun download() {
        val baseFileName = if (properties.siteName.isEmpty()) "output" else properties.siteName
        val outputFile = "${properties.outputDir}${File.separator}${baseFileName}_${properties.startDate}_${properties.endDate}.csv"

        File(properties.outputDir).mkdirs()

        logger.info("Downloading GA data into '$outputFile' in pages of ${properties.maxResults}...")

        CsvListWriter(FileWriter(outputFile), CsvPreference.STANDARD_PREFERENCE).use {

            var currentTotalRows = 0

            while (true) {
                val gaData = fetchGaData(currentTotalRows + 1)

                if (currentTotalRows == 0) {
                    it.writeHeader(*getHeaderNames(gaData), "siteName")
                }

                val rows = gaData.rows

                if (rows == null || rows.isEmpty()) {
                    break
                }

                rows.forEach { row ->
                    it.write(*row.toTypedArray(), properties.siteName)
                }

                currentTotalRows += rows.size

                logger.info("Fetched $currentTotalRows rows out of ${gaData.totalResults}")
            }
        }
    }

    private fun getHeaderNames(gaData: GaData): Array<String> {
        return gaData.columnHeaders
            .map { it.name.replace("ga:", "") }
            .toTypedArray()
    }

    private fun fetchGaData(startIndex: Int): GaData {
        return analytics.data().ga()
            .get(properties.viewId, properties.startDate, properties.endDate, properties.metrics)
            .setDimensions(properties.dimensions)
            .setStartIndex(startIndex)
            .setMaxResults(properties.maxResults)
            .execute()
    }

}
