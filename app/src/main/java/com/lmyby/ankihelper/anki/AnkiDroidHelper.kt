package com.lmyby.ankihelper.anki

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ichi2.anki.api.AddContentApi
import com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION
import com.ichi2.anki.api.NoteInfo
import com.lmyby.ankihelper.util.Constant
import java.util.LinkedList

/**
 * Helper class for interacting with AnkiDroid API
 * Manages permissions, deck/model references, and duplicate detection
 * Converted to Kotlin as part of Phase 13 final conversion
 */
class AnkiDroidHelper(context: Context) {

    private val mContext: Context = context.applicationContext
    val api: AddContentApi = AddContentApi(mContext)

    /**
     * Check if AnkiDroid is running by testing API access
     */
    val isAnkiDroidRunning: Boolean
        get() = api.deckList != null

    /**
     * Start AnkiDroid app
     * @return true if successfully launched, false if AnkiDroid not installed
     */
    fun startAnkiDroid(): Boolean {
        val manager = mContext.packageManager
        val intent = manager.getLaunchIntentForPackage(Constant.ANKI_PACKAGE_NAME) ?: return false

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        mContext.startActivity(intent)
        return true
    }

    /**
     * Whether or not we should request full access to the AnkiDroid API
     */
    fun shouldRequestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        return ContextCompat.checkSelfPermission(mContext, READ_WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request permission from the user to access the AnkiDroid API (for SDK 23+)
     *
     * @param callbackActivity An Activity which implements onRequestPermissionsResult()
     * @param callbackCode     The callback code to be used in onRequestPermissionsResult()
     */
    fun requestPermission(callbackActivity: Activity, callbackCode: Int) {
        ActivityCompat.requestPermissions(callbackActivity, arrayOf(READ_WRITE_PERMISSION), callbackCode)
    }

    /**
     * Save a mapping from deckName to deckId in the SharedPreferences
     */
    fun storeDeckReference(deckName: String, deckId: Long) {
        val decksDb: SharedPreferences = mContext.getSharedPreferences(DECK_REF_DB, Context.MODE_PRIVATE)
        decksDb.edit().putLong(deckName, deckId).apply()
    }

    /**
     * Save a mapping from modelName to modelId in the SharedPreferences
     */
    fun storeModelReference(modelName: String, modelId: Long) {
        val modelsDb: SharedPreferences = mContext.getSharedPreferences(MODEL_REF_DB, Context.MODE_PRIVATE)
        modelsDb.edit().putLong(modelName, modelId).apply()
    }

    /**
     * Remove the duplicates from a list of note fields and tags
     *
     * @param fields  List of fields to remove duplicates from
     * @param tags    List of tags to remove duplicates from
     * @param modelId ID of model to search for duplicates on
     */
    fun removeDuplicates(fields: LinkedList<Array<String>>, tags: LinkedList<Set<String>>, modelId: Long) {
        // Build a list of the duplicate keys (first fields) and find all notes that have a match with each key
        val keys = ArrayList<String>(fields.size)
        for (f in fields) {
            keys.add(f[0])
        }
        val duplicateNotes: SparseArray<List<NoteInfo>> = api.findDuplicateNotes(modelId, keys)

        // Do some sanity checks
        if (tags.size != fields.size) {
            throw IllegalStateException("List of tags must be the same length as the list of fields")
        }
        if (duplicateNotes.size() == 0 || fields.isEmpty() || tags.isEmpty()) {
            return
        }
        if (duplicateNotes.keyAt(duplicateNotes.size() - 1) >= fields.size) {
            throw IllegalStateException("The array of duplicates goes outside the bounds of the original lists")
        }

        // Iterate through the fields and tags LinkedLists, removing those that had a duplicate
        val fieldIterator = fields.listIterator()
        val tagIterator = tags.listIterator()
        var listIndex = -1

        for (i in 0 until duplicateNotes.size()) {
            val duplicateIndex = duplicateNotes.keyAt(i)
            while (listIndex < duplicateIndex) {
                fieldIterator.next()
                tagIterator.next()
                listIndex++
            }
            fieldIterator.remove()
            tagIterator.remove()
        }
    }

    /**
     * Try to find the given model by name, accounting for renaming of the model:
     * If there's a model with this modelName that is known to have previously been created (by this app)
     * and the corresponding model ID exists and has the required number of fields
     * then return that ID (even though it may have since been renamed)
     * If there's a model from getModelList with modelName and required number of fields then return its ID
     * Otherwise return null
     *
     * @param modelName the name of the model to find
     * @param numFields the minimum number of fields the model is required to have
     * @return the model ID or null if not found
     */
    fun findModelIdByName(modelName: String, numFields: Int): Long? {
        val modelsDb = mContext.getSharedPreferences(MODEL_REF_DB, Context.MODE_PRIVATE)
        val prefsModelId = modelsDb.getLong(modelName, -1L)

        // if we have a reference saved to modelName and it exists and has at least numFields then return it
        if (prefsModelId != -1L && api.getModelName(prefsModelId) != null
            && api.getFieldList(prefsModelId).size >= numFields
        ) { // could potentially have been renamed
            return prefsModelId
        }

        val modelList = api.getModelList(numFields)
        for ((key, value) in modelList) {
            if (value == modelName) {
                return key // first model wins
            }
        }

        // model no longer exists (by name nor old id) or the number of fields was reduced
        return null
    }

    /**
     * Try to find the given deck by name, accounting for potential renaming of the deck by the user as follows:
     * If there's a deck with deckName then return its ID
     * If there's no deck with deckName, but a ref to deckName is stored in SharedPreferences, and that deck exists in
     * AnkiDroid (i.e. it was renamed), then use that deck. Note: this deck will not be found if your app is re-installed
     * If there's no reference to deckName anywhere then return null
     *
     * @param deckName the name of the deck to find
     * @return the did of the deck in Anki or null if not found
     */
    fun findDeckIdByName(deckName: String): Long? {
        val decksDb = mContext.getSharedPreferences(DECK_REF_DB, Context.MODE_PRIVATE)

        // Look for deckName in the deck list
        var did = getDeckId(deckName)
        if (did != null) {
            // If the deck was found then return its id
            return did
        } else {
            // Otherwise try to check if we have a reference to a deck that was renamed and return that
            did = decksDb.getLong(deckName, -1)
            if (did != -1L && api.getDeckName(did) != null) {
                return did
            } else {
                // If the deck really doesn't exist then return null
                return null
            }
        }
    }

    /**
     * Get the ID of the deck which matches the name
     *
     * @param deckName Exact name of deck (note: deck names are unique in Anki)
     * @return the ID of the deck that has given name, or null if no deck was found
     */
    fun getDeckId(deckName: String): Long? {
        val deckList = api.deckList
        for ((key, value) in deckList) {
            if (value == deckName) {
                return key
            }
        }
        return null
    }

    companion object {
        private const val DECK_REF_DB = "com.ichi2.anki.api.decks"
        private const val MODEL_REF_DB = "com.ichi2.anki.api.models"

        /**
         * Whether or not the API is available to use.
         * The API could be unavailable if AnkiDroid is not installed or the user explicitly disabled the API
         *
         * @return true if the API is available to use
         */
        @JvmStatic
        fun isApiAvailable(context: Context): Boolean {
            return AddContentApi.getAnkiDroidPackageName(context) != null
        }
    }
}
