package io.suryawirawan.henry.ga.model

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@ConfigurationProperties("ga")
data class Properties(

    var siteName: String = "",
    var viewId: String = "",
    var metrics: String = "",
    var dimensions: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var clientCredentialsFile: String = "",
    var maxResults: Int = 500) {

    @PostConstruct
    fun init() {
        val DATE_FORMAT = "yyyy-MM-dd"
        val yesterdayDateText = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))

        if (startDate.isEmpty()) {
            startDate = yesterdayDateText
        }

        if (endDate.isEmpty()) {
            endDate = yesterdayDateText
        }
    }

    override fun toString(): String {
        return """

        siteName: $siteName
        viewId: $viewId
        metrics: $metrics
        dimensions: $dimensions
        startDate: $startDate
        endDate: $endDate
        clientCredentialsFile: $clientCredentialsFile
        maxResults: $maxResults

        """.trimIndent()
    }

}
