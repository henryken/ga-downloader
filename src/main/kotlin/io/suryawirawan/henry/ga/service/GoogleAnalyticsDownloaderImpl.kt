package io.suryawirawan.henry.ga.service

import com.google.api.services.analytics.Analytics
import io.suryawirawan.henry.ga.model.Properties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GoogleAnalyticsDownloaderImpl : GoogleAnalyticsDownloader {

    @Autowired
    lateinit var analytics: Analytics

    @Autowired
    lateinit var properties: Properties

    override fun download() {
        val gaData =
            analytics.data().ga()
                .get(properties.viewId, properties.startDate, properties.endDate, properties.metrics)
                .setDimensions(properties.dimensions)
                .setStartIndex(1)
                .setMaxResults(500)
                .execute()

        println(gaData.columnHeaders)
    }

}