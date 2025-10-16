package com.lmyby.ankihelper.data.quote

import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.util.com.baidu.translate.demo.HttpGet
import org.json.JSONException
import org.json.JSONObject

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object RandomQuote {
    private const val URL = "https://talaikis.com/api/quotes/random/"

    @JvmStatic
    @Throws(JSONException::class)
    fun fetch(): Quote {
        val doc = HttpGet.get(URL, null)
        val docJson = JSONObject(doc)
        val quote = docJson.getString("quote")
        val author = docJson.getString("author")
        val cat = docJson.getString("cat")
        return Quote(
            Quote = quote,
            Author = author,
            Caption = cat
        )
    }

    @JvmStatic
    fun fetchFromDB(): Quote {
        val quote = QuoteDb.getInstance(MyApplication.getContext()).getQuote()
        val splited = quote.split("\t")
        return Quote(
            Quote = splited[0].replace("<br/>", "\n").trim(),
            Author = splited[1].replace(",", "").trim(),
            Caption = splited[2].replace("\n", "").trim()
        )
    }
}
