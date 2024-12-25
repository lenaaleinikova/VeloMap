package com.example.velomap.data.network

import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class CustomTrustManager {
    fun getCustomTrustManager(certInputStream: InputStream): X509TrustManager {
        // Загрузите сертификат в Keystore
        val cf = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(certInputStream)
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("server", cert)

        // Создайте TrustManagerFactory для использования с SSLContext
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        // Получите TrustManager и верните
        val trustManagers = tmf.trustManagers
        return trustManagers[0] as X509TrustManager
    }

    fun createSslContextWithCustomTrustManager(certInputStream: InputStream): SSLContext {
        val trustManager = getCustomTrustManager(certInputStream)

        // Настройте SSLContext
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        return sslContext
    }

}