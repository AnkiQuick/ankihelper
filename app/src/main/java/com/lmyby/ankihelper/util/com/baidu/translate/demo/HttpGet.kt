package com.lmyby.ankihelper.util.com.baidu.translate.demo

import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object HttpGet {
    private const val SOCKET_TIMEOUT = 10000 // 10S
    private const val GET = "GET"

    @JvmStatic
    fun get(host: String, params: Map<String, String>?): String {
        return try {
            // 设置SSLContext
            val sslcontext = SSLContext.getInstance("TLS")
            sslcontext.init(null, arrayOf<TrustManager>(myX509TrustManager), null)

            val sendUrl = getUrlWithQueryString(host, params)

            // System.out.println("URL:" + sendUrl);

            val uri = URL(sendUrl) // 创建URL对象
            val conn = uri.openConnection() as HttpURLConnection
            if (conn is HttpsURLConnection) {
                conn.sslSocketFactory = sslcontext.socketFactory
            }

            conn.connectTimeout = SOCKET_TIMEOUT // 设置相应超时
            conn.requestMethod = GET
            val statusCode = conn.responseCode
            if (statusCode != HttpURLConnection.HTTP_OK) {
                println("Http错误码：$statusCode")
            }

            // 读取服务器的数据
            val text = conn.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { br ->
                    val builder = StringBuilder()
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        builder.append(line)
                    }
                    builder.toString()
                }
            }

            conn.disconnect() // 断开连接

            text
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            ""
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            ""
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }

    @JvmStatic
    fun getUrlWithQueryString(url: String, params: Map<String, String>?): String {
        if (params == null) {
            return url
        }

        val builder = StringBuilder(url)
        if (url.contains("?")) {
            builder.append("&")
        } else {
            builder.append("?")
        }

        var i = 0
        for ((key, value) in params) {
            if (i != 0) {
                builder.append('&')
            }

            builder.append(key)
            builder.append('=')
            builder.append(encode(value))

            i++
        }

        return builder.toString()
    }

    private fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 对输入的字符串进行URL编码, 即转换为%20这种形式
     * @param input 原文
     * @return URL编码. 如果编码失败, 则返回原文
     */
    @JvmStatic
    fun encode(input: String?): String {
        if (input == null) {
            return ""
        }

        return try {
            URLEncoder.encode(input, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            input
        }
    }

    private val myX509TrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return null
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    }
}
