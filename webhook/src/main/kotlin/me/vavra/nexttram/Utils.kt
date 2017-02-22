package me.vavra.nexttram

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomUtils
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.logging.Logger

/**
 * Helper functions.
 */

fun download(url: String): String? {
    val connection = URL(url).openConnection() as HttpURLConnection
    val respCode = connection.responseCode
    if (respCode == HttpURLConnection.HTTP_OK) {
        return connection.inputStream.readToString()
    }
    return null
}

fun InputStream.readToString(): String {
    val response = IOUtils.toString(this, "UTF-8")
    IOUtils.closeQuietly(this)
    return response
}

fun getProperty(javaClass: Class<Any>, propertyName: String): String? {
    val props = Properties()
    props.load(javaClass.classLoader.getResourceAsStream("config.properties"))
    return props.getProperty(propertyName)
}

fun l(what: Any?) {
    Logger.getLogger(WebHook::class.java.name).warning(what.toString())
}

fun <E> List<E>.randomElement(): E {
    return this[RandomUtils.nextInt(0, this.size)]
}

fun String.urlEncode(): String {
    return URLEncoder.encode(this, "UTF-8")
}
