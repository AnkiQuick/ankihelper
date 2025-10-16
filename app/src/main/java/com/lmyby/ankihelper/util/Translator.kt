package com.lmyby.ankihelper.util

import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.util.com.baidu.translate.demo.RandomAPIKeyGenerator
import com.lmyby.ankihelper.util.com.baidu.translate.demo.TransApi
import org.json.JSONException
import org.json.JSONObject

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object Translator {
    private const val APP_ID = "20160220000012831"
    private const val SECURITY_KEY = "ISSPx0K_ZyrUN9IAOKel"
    private var api: TransApi? = null

    @JvmStatic
    fun translate(query: String, from: String, to: String): String {
        // Remove line break
        // query = query.replaceAll("\n","")
        if (api == null) {
            val settings = Settings.getInstance(MyApplication.getContext())
            api = if (settings.userBaidufanyiAppId.isEmpty()) {
                val appAndKey = RandomAPIKeyGenerator.next()
                TransApi(appAndKey[0], appAndKey[1])
            } else {
                val id = settings.userBaidufanyiAppId
                val key = settings.userBaidufanyiAppKey
                TransApi(id, key)
            }
        }

        return try {
            val jsonStr = api!!.getTransResult(query, from, to)
            val json = JSONObject(jsonStr)
            val resultArray = json.getJSONArray("trans_result")
            val sb = StringBuilder()
            for (i in 0 until resultArray.length() - 1) {
                sb.append(resultArray.getJSONObject(i).getString("dst"))
                sb.append("\n")
            }
            if (resultArray.length() > 0) {
                sb.append(resultArray.getJSONObject(resultArray.length() - 1).getString("dst"))
            }
            sb.toString()
        } catch (e: JSONException) {
            // Toast.makeText(MyApplication.getContext(), e.getMessage() + jsonStr, Toast.LENGTH_LONG).show();
            "error\n${e.message}\n"
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // TransApi api = new TransApi(APP_ID, SECURITY_KEY);
        // String query = "高度600米";
        // System.out.println(api.getTransResult(query, "auto", "cn"));
        println(translate("i am a big fat guy", "auto", "zh"))
    }
}
