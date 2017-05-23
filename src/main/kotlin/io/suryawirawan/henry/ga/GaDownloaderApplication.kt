package io.suryawirawan.henry.ga

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils
import com.google.api.services.analytics.Analytics
import com.google.api.services.analytics.AnalyticsScopes
import io.suryawirawan.henry.ga.model.Properties
import io.suryawirawan.henry.ga.service.GoogleAnalyticsDownloader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.io.StringReader
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

@SpringBootApplication
@EnableConfigurationProperties(Properties::class)
class GaDownloaderApplication : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(GaDownloaderApplication::class.java)

    @Autowired
    lateinit var properties: Properties

    @Autowired
    lateinit var googleAnalyticsDownloader: GoogleAnalyticsDownloader

    @Bean
    fun analytics(): Analytics {
        val clientCredentials = Parser().parse(properties.clientCredentialsFile) as JsonObject
        val privateKey = clientCredentials.get("private_key") as String
        val clientEmail = clientCredentials.get("client_email") as String

        val JSON_FACTORY = JacksonFactory.getDefaultInstance()
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

        val privateKeyBytes =
            PemReader.readFirstSectionAndClose(StringReader(privateKey), "PRIVATE KEY").base64DecodedBytes

        val pk = SecurityUtils.getRsaKeyFactory().generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

        val googleCredential =
            GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(clientEmail)
                .setServiceAccountPrivateKey(pk)
                .setServiceAccountScopes(Collections.singleton(AnalyticsScopes.ANALYTICS_READONLY))
                .build()

        return Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
            .setHttpRequestInitializer({ httpRequest ->
                googleCredential.initialize(httpRequest)
                httpRequest.connectTimeout = 60000
                httpRequest.readTimeout = 60000
            })
            .setApplicationName("GA-Downloader")
            .build()
    }

    override fun run(vararg args: String?) {
        logger.info("===========================")
        logger.info("Google Analytics Downloader")
        logger.info("===========================")

        logger.info(properties.toString())

        googleAnalyticsDownloader.download()
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(GaDownloaderApplication::class.java, *args)
}
