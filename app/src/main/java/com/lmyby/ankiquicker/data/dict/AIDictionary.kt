package com.lmyby.ankiquicker.data.dict

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.ai.AIDictionaryConfig
import com.lmyby.ankiquicker.data.ai.AIConfigRepository
import com.lmyby.ankiquicker.data.ai.AIException
import com.lmyby.ankiquicker.data.ai.service.AIDictionaryService

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class AIDictionary(private val config: AIDictionaryConfig) : IDictionary {
    private val service: AIDictionaryService = AIDictionaryService()

    override fun getDictionaryKey(): String {
        return "AI_${config.id}"
    }

    override fun getDictionaryName(): String {
        return config.dictionaryName ?: "AI Dictionary"
    }

    override fun getIntroduction(): String {
        val llmConfig = AIConfigRepository.getLLMConfigById(config.llmId)
        return if (llmConfig != null) {
            "AI Dictionary using ${llmConfig.name} model"
        } else {
            "AI Dictionary"
        }
    }

    /**
     * Get export elements list with localized field names
     * @return Array of localized field names
     */
    override fun getExportElementsList(): Array<String> {
        val context = MyApplication.getContext()
        return DICT_FIELD_RES_IDS.map { context.getString(it) }.toTypedArray()
    }

    override fun wordLookup(key: String): List<Definition> {
        val definitions = mutableListOf<Definition>()

        try {
            Log.d("AIDictionary", "Starting word lookup for: $key")

            // Get the LLM config
            val llmConfig = AIConfigRepository.getLLMConfigById(config.llmId)
            if (llmConfig == null) {
                Log.w("AIDictionary", "LLM config not found for config ID: ${config.llmId}")
                return definitions
            }

            Log.d("AIDictionary", "LLM config found: ${llmConfig.name}")

            // Get the word definition from the AI service
            val cacheResults = service.getWordDefinition(key, config, llmConfig)

            Log.d("AIDictionary", "Received ${cacheResults.size} cache results")

            // Get localized field names
            val fieldNames = getExportElementsList()

            // Convert AIDictionaryCache results to Definition objects
            for (cache in cacheResults) {
                // Create export elements map
                val exportElements = mutableMapOf<String, String>()
                val determinedWord = cache.hwd ?: key
                val sense = cache.sense ?: ""
                val phrase = cache.phrase ?: ""
                val phonetics = cache.phonetics ?: ""
                val defEn = cache.defEn ?: ""
                val defCn = cache.defCn ?: ""

                exportElements[fieldNames[0]] = determinedWord
                exportElements[fieldNames[1]] =
                    "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>"
                exportElements[fieldNames[2]] = "<span >$phonetics</span>"

                if (phrase.isEmpty()) {
                    exportElements[fieldNames[3]] =
                        "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span>"

                    exportElements[fieldNames[4]] =
                        "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>"
                } else {
                    exportElements[fieldNames[3]] =
                        "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span>"

                    exportElements[fieldNames[4]] =
                        "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>"
                }

                exportElements[fieldNames[5]] = getYoudaoAudioTag(determinedWord, 2)
                exportElements[fieldNames[6]] = getYoudaoAudioTag(determinedWord, 1)

                // Create display HTML
                val displayHtml = buildString {
                    if (phrase.isEmpty()) {
                        append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense </span> &nbsp;<span> $phonetics</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>")
                    } else {
                        append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$phrase </span>&nbsp; </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>$sense </span> &nbsp; <span> $phonetics</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>$defEn</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>$defCn</span>")
                    }
                }

                // Create Definition object
                val def = Definition(exportElements, displayHtml)
                definitions.add(def)
            }

            Log.d("AIDictionary", "Returning ${definitions.size} definitions")
        } catch (e: AIException) {
            Log.e("AIDictionary", "AI Error during dictionary lookup for word: $key", e)
            // Create a Definition object to show the error to the user
            val exportElements = mutableMapOf("Error" to "AI Dictionary Error: ${e.message}")
            val errorDef = Definition(
                exportElements,
                "<b>AI Dictionary Error</b><br/>${e.message}<br/><br/>Please check your LLM configuration and ensure the API is accessible."
            )
            definitions.add(errorDef)
        } catch (e: Exception) {
            Log.e("AIDictionary", "Error during dictionary lookup for word: $key", e)
            e.printStackTrace()
            // Create a Definition object to show the error to the user
            val exportElements = mutableMapOf("Error" to "Error: ${e.message}")
            val errorDef = Definition(
                exportElements,
                "<b>Error</b><br/>${e.message}<br/><br/>Please check the logs for more details."
            )
            definitions.add(errorDef)
        }

        return definitions
    }

    private fun getYoudaoAudioTag(word: String, type: Int): String {
        return String.format("[sound:https://dict.youdao.com/dictvoice?audio=%s&type=%d]", word, type)
    }

    override fun getAutoCompleteAdapter(context: Context, layout: Int): ListAdapter {
        // Return a simple adapter with no autocomplete for AI dictionaries
        return ArrayAdapter(context, layout, emptyArray<String>())
    }

    fun getConfigId(): Long {
        return config.id
    }

    companion object {
        // Resource IDs for internationalization
        private val DICT_FIELD_RES_IDS = intArrayOf(
            R.string.dict_field_word,
            R.string.dict_field_pos,
            R.string.dict_field_phonetic,
            R.string.dict_field_en_definition,
            R.string.dict_field_zh_definition,
            R.string.dict_field_us_pronunciation,
            R.string.dict_field_uk_pronunciation
        )
    }
}
