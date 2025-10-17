package com.lmyby.ankiquicker.data.dict

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.lmyby.ankiquicker.MyApplication
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class UrbanAutoCompleteAdapter(
    context: Context,
    textViewResourceId: Int
) : ArrayAdapter<String>(context, textViewResourceId), Filterable {

    private var resultList: ArrayList<String>? = null

    override fun getCount(): Int {
        return resultList?.size ?: 0
    }

    override fun getItem(index: Int): String? {
        return resultList?.get(index)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString())

                    // Assign the data to the FilterResults
                    filterResults.values = resultList
                    filterResults.count = resultList!!.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun autocomplete(key: String): ArrayList<String> {
        if (key.trim().isEmpty()) {
            return ArrayList()
        }
        val url = BASE_URL + key
        MyApplication.getOkHttpClient().dispatcher.cancelAll()
        val request = Request.Builder().url(url).build()
        return try {
            val json = MyApplication.getOkHttpClient().newCall(request).execute().body!!.string()
            val result = ArrayList<String>()
            val jsonObject = JSONObject(json)
            val reItems = jsonObject.getJSONArray("results")
            for (i in 0 until reItems.length()) {
                val term = reItems.getJSONObject(i).getString("term")
                result.add(term)
            }
            result
        } catch (ioe: IOException) {
            ArrayList()
        } catch (je: JSONException) {
            ArrayList()
        }
    }

    companion object {
        private const val BASE_URL = "https://api.urbandictionary.com/v0/autocomplete-extra?term="
    }
}
