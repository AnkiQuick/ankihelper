/***************************************************************************************
 *                                                                                      *
 * Copyright (c) 2015 Timothy Rae <perceptualchaos2@gmail.com>                          *
 * Copyright (c) 2016 Mark Carter <mark@marcardar.com>                                  *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU Lesser General Public License as published by the Free Software *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU Lesser General Public License along with  *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.anki.api

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.util.SparseArray
import com.ichi2.anki.FlashCardsContract
import com.ichi2.anki.FlashCardsContract.Card
import com.ichi2.anki.FlashCardsContract.CardTemplate
import com.ichi2.anki.FlashCardsContract.Deck
import com.ichi2.anki.FlashCardsContract.Model
import com.ichi2.anki.FlashCardsContract.Note
import com.lmyby.ankiquicker.R
import java.util.Locale

/**
 * API which can be used to add and query notes, cards, decks, and models to AnkiDroid
 *
 * On Android M (and higher) the [READ_WRITE_PERMISSION] is required for all read/write operations.
 * On earlier SDK levels, the [READ_WRITE_PERMISSION] is currently only required for update/delete operations but
 * this may be extended to all operations at a later date.
 *
 * Converted to Kotlin as part of package rename migration
 */
class AddContentApi(context: Context) {
    private val mResolver: ContentResolver
    private val mContext: Context

    init {
        mContext = context.applicationContext
        mResolver = mContext.contentResolver
    }

    /**
     * Create a new note with specified fields, tags, and model and place it in the specified deck.
     * No duplicate checking is performed - so the note should be checked beforehand using [findDuplicateNotes]
     * @param modelId ID for the model used to add the notes
     * @param deckId ID for the deck the cards should be stored in (use [DEFAULT_DECK_ID] for default deck)
     * @param fields fields to add to the note. Length should be the same as number of fields in model
     * @param tags tags to include in the new note
     * @return note id or null if the note could not be added
     */
    fun addNote(modelId: Long, deckId: Long, fields: Array<String>, tags: Set<String>?): Long? {
        val noteUri = addNoteInternal(modelId, deckId, fields, tags) ?: return null
        return noteUri.lastPathSegment?.toLongOrNull()
    }

    private fun addNoteInternal(modelId: Long, deckId: Long, fields: Array<String>, tags: Set<String>?): Uri? {
        val values = ContentValues().apply {
            put(Note.MID, modelId)
            put(Note.FLDS, Utils.joinFields(fields))
            tags?.let { put(Note.TAGS, Utils.joinTags(it)) }
        }
        return addNoteForContentValues(deckId, values)
    }

    private fun addNoteForContentValues(deckId: Long, values: ContentValues): Uri? {
        val newNoteUri = try {
            mResolver.insert(Note.CONTENT_URI, values)
        } catch (e: Exception) {
            com.lmyby.ankiquicker.util.Utils.showMessage(
                mContext,
                mContext.getString(R.string.str_check_ankidroid_permisson)
            )
            return null
        } ?: return null

        // Move cards to specified deck
        val cardsUri = Uri.withAppendedPath(newNoteUri, "cards")
        val cardsCursor = mResolver.query(cardsUri, null, null, null, null) ?: return null

        cardsCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val columnIndex = cursor.getColumnIndex(Card.CARD_ORD)
                if (columnIndex != -1) {
                    val ord = cursor.getString(columnIndex)
                    val cardValues = ContentValues().apply {
                        put(Card.DECK_ID, deckId)
                    }
                    val cardUri = Uri.withAppendedPath(Uri.withAppendedPath(newNoteUri, "cards"), ord)
                    mResolver.update(cardUri, cardValues, null, null)
                }
            }
        }
        return newNoteUri
    }

    /**
     * Create new notes with specified fields, tags and model and place them in the specified deck.
     * No duplicate checking is performed - so all notes should be checked beforehand using [findDuplicateNotes]
     * @param modelId id for the model used to add the notes
     * @param deckId id for the deck the cards should be stored in (use [DEFAULT_DECK_ID] for default deck)
     * @param fieldsList List of fields arrays (one per note). Array lengths should be same as number of fields in model
     * @param tagsList List of tags (one per note) (may be null)
     * @return The number of notes added (<0 means there was a problem)
     */
    fun addNotes(modelId: Long, deckId: Long, fieldsList: List<Array<String>>, tagsList: List<Set<String>?>?): Int {
        if (tagsList != null && fieldsList.size != tagsList.size) {
            throw IllegalArgumentException("fieldsList and tagsList different length")
        }

        val newNoteValuesList = fieldsList.mapIndexed { i, fields ->
            ContentValues().apply {
                put(Note.MID, modelId)
                put(Note.FLDS, Utils.joinFields(fields))
                tagsList?.get(i)?.let { tags ->
                    put(Note.TAGS, Utils.joinTags(tags))
                }
            }
        }

        if (newNoteValuesList.isEmpty()) {
            return 0
        }
        return compat.insertNotes(deckId, newNoteValuesList.toTypedArray())
    }

    /**
     * Find all existing notes in the collection which have mid and a duplicate key
     * @param mid model id
     * @param key the first field of a note
     * @return a list of duplicate notes
     */
    fun findDuplicateNotes(mid: Long, key: String): List<NoteInfo> {
        val notes = compat.findDuplicateNotes(mid, listOf(key))
        return if (notes.size() == 0) emptyList() else notes.valueAt(0)
    }

    /**
     * Find all notes in the collection which have mid and a first field that matches key
     * Much faster than calling findDuplicateNotes(long, String) when the list of keys is large
     * @param mid model id
     * @param keys list of keys
     * @return a SparseArray with a list of duplicate notes for each key
     */
    fun findDuplicateNotes(mid: Long, keys: List<String>): SparseArray<List<NoteInfo>> {
        return compat.findDuplicateNotes(mid, keys)
    }

    /**
     * Get the number of notes that exist for the specified model ID
     * @param mid id of the model to be used
     * @return number of notes that exist with that model ID or -1 if there was a problem
     */
    fun getNoteCount(mid: Long): Int {
        val cursor = compat.queryNotes(mid) ?: return 0
        return cursor.use { it.count }
    }

    /**
     * Set the tags for a given note
     * @param noteId the ID of the note to update
     * @param tags set of tags
     * @return true if noteId was found, otherwise false
     * @throws SecurityException if READ_WRITE_PERMISSION not granted (e.g. due to install order bug)
     */
    fun updateNoteTags(noteId: Long, tags: Set<String>): Boolean {
        return updateNote(noteId, null, tags)
    }

    /**
     * Set the fields for a given note
     * @param noteId the ID of the note to update
     * @param fields array of fields
     * @return true if noteId was found, otherwise false
     * @throws SecurityException if READ_WRITE_PERMISSION not granted (e.g. due to install order bug)
     */
    fun updateNoteFields(noteId: Long, fields: Array<String>): Boolean {
        return updateNote(noteId, fields, null)
    }

    /**
     * Get the contents of a note with known ID
     * @param noteId the ID of the note to find
     * @return object containing the contents of note with noteID or null if there was a problem
     */
    fun getNote(noteId: Long): NoteInfo? {
        val noteUri = Uri.withAppendedPath(Note.CONTENT_URI, noteId.toString())
        val cursor = mResolver.query(noteUri, PROJECTION, null, null, null) ?: return null
        return cursor.use {
            if (it.moveToNext()) NoteInfo.buildFromCursor(it) else null
        }
    }

    private fun updateNote(noteId: Long, fields: Array<String>?, tags: Set<String>?): Boolean {
        val contentUri = Note.CONTENT_URI.buildUpon()
            .appendPath(noteId.toString())
            .build()
        val values = ContentValues().apply {
            fields?.let { put(Note.FLDS, Utils.joinFields(it)) }
            tags?.let { put(Note.TAGS, Utils.joinTags(it)) }
        }
        val numRowsUpdated = mResolver.update(contentUri, values, null, null)
        // provider doesn't check whether fields actually changed, so just returns number of notes with id == noteId
        return numRowsUpdated > 0
    }

    /**
     * Get the html that would be generated for the specified note type and field list
     * @param flds array of field values for the note. Length must be the same as num. fields in mid.
     * @param mid id for the note type to be used
     * @return list of front & back pairs for each card which contain the card HTML, or null if there was a problem
     * @throws SecurityException if READ_WRITE_PERMISSION not granted (e.g. due to install order bug)
     */
    fun previewNewNote(mid: Long, flds: Array<String>): Map<String, Map<String, String>>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !hasReadWritePermission()) {
            // avoid situation where addNote will pass, but deleteNote will fail
            throw SecurityException("previewNewNote requires full read-write-permission")
        }
        val newNoteUri = addNoteInternal(mid, DEFAULT_DECK_ID, flds, setOf(TEST_TAG)) ?: return null

        // Build map of HTML for each generated card
        val cards = mutableMapOf<String, Map<String, String>>()
        val cardsUri = Uri.withAppendedPath(newNoteUri, "cards")
        val cardsCursor = mResolver.query(cardsUri, null, null, null, null) ?: return null

        cardsCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val nameIndex = cursor.getColumnIndex(Card.CARD_NAME)
                val answerIndex = cursor.getColumnIndex(Card.ANSWER)
                val questionIndex = cursor.getColumnIndex(Card.QUESTION)

                if (nameIndex != -1 && answerIndex != -1) {
                    val n = cursor.getString(nameIndex)
                    val q = cursor.getString(questionIndex)
                    val a = cursor.getString(answerIndex)
                    cards[n] = mapOf("q" to q, "a" to a)
                }
            }
        }
        // Delete the note
        mResolver.delete(newNoteUri, null, null)
        return cards
    }

    /**
     * Insert a new basic front/back model with two fields and one card
     * @param name name of the model
     * @return the mid of the model which was created, or null if it could not be created
     */
    fun addNewBasicModel(name: String): Long? {
        return addNewCustomModel(
            name,
            BasicModel.FIELDS,
            BasicModel.CARD_NAMES,
            BasicModel.QFMT,
            BasicModel.AFMT,
            null,
            null,
            null
        )
    }

    /**
     * Insert a new basic front/back model with two fields and TWO cards
     * The first card goes from front->back, and the second goes from back->front
     * @param name name of the model
     * @return the mid of the model which was created, or null if it could not be created
     */
    fun addNewBasic2Model(name: String): Long? {
        return addNewCustomModel(
            name,
            Basic2Model.FIELDS,
            Basic2Model.CARD_NAMES,
            Basic2Model.QFMT,
            Basic2Model.AFMT,
            null,
            null,
            null
        )
    }

    /**
     * Insert a new model into AnkiDroid.
     * See the [Anki Desktop Manual](http://ankisrs.net/docs/manual.html#cards-and-templates) for more help
     * @param name: name of model
     * @param fields: array of field names
     * @param cards: array of names for the card templates
     * @param qfmt: array of formatting strings for the question side of each template in cards
     * @param afmt: array of formatting strings for the answer side of each template in cards
     * @param css: css styling information to be shared across all of the templates. Use null for default CSS.
     * @param did: default deck to add cards to when using this model. Use null or [DEFAULT_DECK_ID] for default deck.
     * @param sortf: index of field to be used for sorting. Use null for unspecified (unsupported in provider spec v1)
     * @return the mid of the model which was created, or null if it could not be created
     */
    fun addNewCustomModel(
        name: String,
        fields: Array<String>,
        cards: Array<String>,
        qfmt: Array<String>,
        afmt: Array<String>,
        css: String?,
        did: Long?,
        sortf: Int?
    ): Long? {
        // Check that size of arrays are consistent
        if (qfmt.size != cards.size || afmt.size != cards.size) {
            throw IllegalArgumentException("cards, qfmt, and afmt arrays must all be same length")
        }

        // Create the model using dummy templates
        val values = ContentValues().apply {
            put(Model.NAME, name)
            put(Model.FIELD_NAMES, Utils.joinFields(fields))
            put(Model.NUM_CARDS, cards.size)
            put(Model.CSS, css)
            put(Model.DECK_ID, did)
            put(Model.SORT_FIELD_INDEX, sortf)
        }
        val modelUri = mResolver.insert(Model.CONTENT_URI, values) ?: return null

        // Set the remaining template parameters
        val templatesUri = Uri.withAppendedPath(modelUri, "templates")
        for (i in cards.indices) {
            val uri = Uri.withAppendedPath(templatesUri, i.toString())
            val templateValues = ContentValues().apply {
                put(CardTemplate.NAME, cards[i])
                put(CardTemplate.QUESTION_FORMAT, qfmt[i])
                put(CardTemplate.ANSWER_FORMAT, afmt[i])
            }
            mResolver.update(uri, templateValues, null, null)
        }
        return modelUri.lastPathSegment?.toLongOrNull()
    }

    /**
     * Get the ID for the note type / model which is currently in use
     * @return id for current model, or <0 if there was a problem
     */
    fun getCurrentModelId(): Long {
        val uri = Uri.withAppendedPath(Model.CONTENT_URI, Model.CURRENT_MODEL_ID)
        val singleModelCursor = mResolver.query(uri, null, null, null, null) ?: return -1L

        return singleModelCursor.use { cursor ->
            cursor.moveToFirst()
            val modelIdIndex = cursor.getColumnIndex(Model._ID)
            if (modelIdIndex != -1) cursor.getLong(modelIdIndex) else -1L
        }
    }

    /**
     * Get the field names belonging to specified model
     * @param modelId the ID of the model to use
     * @return the names of all the fields, or null if the model doesn't exist or there was some other problem
     */
    fun getFieldList(modelId: Long): Array<String>? {
        val uri = Uri.withAppendedPath(Model.CONTENT_URI, modelId.toString())
        val modelCursor = mResolver.query(uri, null, null, null, null) ?: return null

        return modelCursor.use { cursor ->
            if (cursor.moveToNext()) {
                val fieldNamesIndex = cursor.getColumnIndex(Model.FIELD_NAMES)
                if (fieldNamesIndex != -1) {
                    val flds = cursor.getString(fieldNamesIndex)
                    Utils.splitFields(flds)
                } else {
                    emptyArray()
                }
            } else {
                null
            }
        }
    }

    /**
     * Get a map of all model ids and names
     * @return map of (id, name) pairs
     */
    fun getModelList(): Map<Long, String>? {
        return getModelList(1)
    }

    /**
     * Get a map of all model ids and names with number of fields larger than minNumFields
     * @param minNumFields minimum number of fields to consider the model for inclusion
     * @return map of (id, name) pairs or null if there was a problem
     */
    fun getModelList(minNumFields: Int): Map<Long, String>? {
        android.util.Log.e("AddContentApi", "getModelList: Starting query with minNumFields=$minNumFields...")
        val allModelsCursor = mResolver.query(Model.CONTENT_URI, null, null, null, null)

        if (allModelsCursor == null) {
            android.util.Log.e("AddContentApi", "getModelList: Cursor is NULL - permission issue?")
            return null
        }

        android.util.Log.e("AddContentApi", "getModelList: Cursor count = ${allModelsCursor.count}")
        val models = mutableMapOf<Long, String>()

        allModelsCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val modelIdIndex = cursor.getColumnIndex(Model._ID)
                val nameIndex = cursor.getColumnIndex(Model.NAME)
                val fieldNamesIndex = cursor.getColumnIndex(Model.FIELD_NAMES)

                if (modelIdIndex != -1 && nameIndex != -1 && fieldNamesIndex != -1) {
                    val modelId = cursor.getLong(modelIdIndex)
                    val name = cursor.getString(nameIndex)
                    val flds = cursor.getString(fieldNamesIndex)
                    val numFlds = Utils.splitFields(flds)?.size ?: 0

                    android.util.Log.e(
                        "AddContentApi",
                        "getModelList: Found model: id=$modelId, name=$name, numFields=$numFlds"
                    )

                    if (numFlds >= minNumFields) {
                        models[modelId] = name
                    }
                }
            }
        }
        android.util.Log.e("AddContentApi", "getModelList: Returning ${models.size} models")
        return models
    }

    /**
     * Get the name of the model which has given ID
     * @param mid id of model
     * @return the name of the model, or null if no model was found
     */
    fun getModelName(mid: Long?): String? {
        if (mid == null || mid < 0) return null
        val modelList = getModelList() ?: return null
        return modelList[mid]
    }

    /**
     * Create a new deck with specified name and save the reference to SharedPreferences for later
     * @param deckName name of the deck to add
     * @return id of the added deck, or null if the deck was not added
     */
    fun addNewDeck(deckName: String): Long? {
        val values = ContentValues().apply {
            put(Deck.DECK_NAME, deckName)
        }
        val newDeckUri = mResolver.insert(Deck.CONTENT_ALL_URI, values) ?: return null
        return newDeckUri.lastPathSegment?.toLongOrNull()
    }

    /**
     * Get the name of the selected deck
     * @return deck name or null if there was a problem
     */
    fun getSelectedDeckName(): String? {
        val selectedDeckCursor = mResolver.query(Deck.CONTENT_SELECTED_URI, null, null, null, null)
            ?: return null

        return selectedDeckCursor.use { cursor ->
            if (cursor.moveToNext()) {
                val nameIndex = cursor.getColumnIndex(Deck.DECK_NAME)
                if (nameIndex != -1) cursor.getString(nameIndex) else null
            } else {
                null
            }
        }
    }

    /**
     * Get a list of all the deck id / name pairs
     * @return Map of (id, name) pairs, or null if there was a problem
     */
    fun getDeckList(): Map<Long, String>? {
        android.util.Log.e("AddContentApi", "getDeckList: Starting query...")
        val allDecksCursor = mResolver.query(Deck.CONTENT_ALL_URI, null, null, null, null)

        if (allDecksCursor == null) {
            android.util.Log.e("AddContentApi", "getDeckList: Cursor is NULL - permission issue?")
            return null
        }

        android.util.Log.e("AddContentApi", "getDeckList: Cursor count = ${allDecksCursor.count}")
        val decks = mutableMapOf<Long, String>()

        allDecksCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val deckIdIndex = cursor.getColumnIndex(Deck.DECK_ID)
                val nameIndex = cursor.getColumnIndex(Deck.DECK_NAME)

                if (deckIdIndex != -1 && nameIndex != -1) {
                    val deckId = cursor.getLong(deckIdIndex)
                    val name = cursor.getString(nameIndex)
                    decks[deckId] = name
                    android.util.Log.e("AddContentApi", "getDeckList: Found deck: id=$deckId, name=$name")
                }
            }
        }
        android.util.Log.e("AddContentApi", "getDeckList: Returning ${decks.size} decks")
        return decks
    }

    /**
     * Get the name of the deck which has given ID
     * @param did ID of deck
     * @return the name of the deck, or null if no deck was found
     */
    fun getDeckName(did: Long?): String? {
        if (did == null || did < 0) return null
        val deckList = getDeckList() ?: return null
        return deckList[did]
    }

    /**
     * Get the AnkiDroid API spec version of the installed AnkiDroid app.
     * This is not the same as the AnkiDroid app version code.
     *
     * SPEC VERSION 1: (AnkiDroid 2.5)
     * [addNotes] is very slow for large numbers of notes
     * [findDuplicateNotes] is very slow for large numbers of keys
     * [addNewCustomModel] is not persisted properly
     * [addNewCustomModel] does not support sortf argument
     *
     * SPEC VERSION 2: (AnkiDroid 2.6)
     *
     * @return the spec version number or -1 if AnkiDroid is not installed.
     */
    fun getApiHostSpecVersion(): Int {
        // PackageManager#resolveContentProvider docs suggest flags should be 0 (but that gives null metadata)
        // GET_META_DATA seems to work anyway
        val info = mContext.packageManager.resolveContentProvider(
            FlashCardsContract.AUTHORITY,
            PackageManager.GET_META_DATA
        ) ?: return -1

        return if (info.metaData?.containsKey(PROVIDER_SPEC_META_DATA_KEY) == true) {
            info.metaData.getInt(PROVIDER_SPEC_META_DATA_KEY)
        } else {
            DEFAULT_PROVIDER_SPEC_VALUE
        }
    }

    private fun hasReadWritePermission(): Boolean {
        return mContext.checkPermission(READ_WRITE_PERMISSION, Process.myPid(), Process.myUid()) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Best not to store this in case the user updates AnkiDroid app while client app is staying alive
     */
    private val compat: Compat
        get() = if (getApiHostSpecVersion() < 2) CompatV1() else CompatV2()

    private interface Compat {
        /**
         * Query all notes for a given model
         * @param modelId the model ID to limit query to
         * @return a cursor with all notes matching modelId
         */
        fun queryNotes(modelId: Long): Cursor?

        /**
         * Add new notes to the AnkiDroid content provider in bulk.
         * @param deckId the deck ID to put the cards in
         * @param valuesArr the content values ready for bulk insertion into the content provider
         * @return the number of successful entries
         */
        fun insertNotes(deckId: Long, valuesArr: Array<ContentValues>): Int

        /**
         * For each key, look for an existing note that has matching first field
         * @param modelId the model ID to limit the search to
         * @param keys  list of keys for each note
         * @return array with a list of NoteInfo objects for each key if duplicates exist
         */
        fun findDuplicateNotes(modelId: Long, keys: List<String>): SparseArray<List<NoteInfo>>
    }

    private open inner class CompatV1 : Compat {
        override fun queryNotes(modelId: Long): Cursor? {
            val modelName = getModelName(modelId) ?: return null
            val queryFormat = String.format("note:\"%s\"", modelName)
            return mResolver.query(Note.CONTENT_URI, PROJECTION, queryFormat, null, null)
        }

        override fun insertNotes(deckId: Long, valuesArr: Array<ContentValues>): Int {
            var result = 0
            for (values in valuesArr) {
                val noteUri = addNoteForContentValues(deckId, values)
                if (noteUri != null) {
                    result++
                }
            }
            return result
        }

        override fun findDuplicateNotes(modelId: Long, keys: List<String>): SparseArray<List<NoteInfo>> {
            // Content provider spec v1 does not support direct querying of the notes table, so use Anki browser syntax
            val modelName = getModelName(modelId)
            val modelFieldList = getFieldList(modelId)
            if (modelName == null || modelFieldList == null) {
                return SparseArray()
            }

            val duplicates = SparseArray<List<NoteInfo>>()
            // Loop through each item in fieldsArray looking for an existing note, and add it to the duplicates array
            val queryFormat = String.format("%s:\"%%s\" note:\"%s\"", modelFieldList[0], modelName)
            for (outputPos in keys.indices) {
                val selection = String.format(queryFormat, keys[outputPos])
                val cursor = mResolver.query(Note.CONTENT_URI, PROJECTION, selection, null, null)
                    ?: continue

                cursor.use {
                    while (it.moveToNext()) {
                        val note = NoteInfo.buildFromCursor(it) ?: continue
                        addNoteToDuplicatesArray(note, duplicates, outputPos)
                    }
                }
            }
            return duplicates
        }

        /** Add a NoteInfo object to the given duplicates SparseArray at the specified position */
        protected fun addNoteToDuplicatesArray(
            note: NoteInfo,
            duplicates: SparseArray<List<NoteInfo>>,
            position: Int
        ) {
            val sparseArrayIndex = duplicates.indexOfKey(position)
            if (sparseArrayIndex < 0) {
                // No existing NoteInfo objects mapping to same key as the current note so add a new List
                val duplicatesForKey = mutableListOf<NoteInfo>()
                duplicatesForKey.add(note)
                duplicates.put(position, duplicatesForKey)
            } else {
                // Append note to existing list of duplicates for key
                duplicates.valueAt(sparseArrayIndex).let { list ->
                    (list as? MutableList)?.add(note)
                }
            }
        }
    }

    private inner class CompatV2 : CompatV1() {
        override fun queryNotes(modelId: Long): Cursor? {
            return mResolver.query(
                Note.CONTENT_URI_V2,
                PROJECTION,
                String.format(Locale.US, "%s=%d", Note.MID, modelId),
                null,
                null
            )
        }

        override fun insertNotes(deckId: Long, valuesArr: Array<ContentValues>): Int {
            val builder = Note.CONTENT_URI.buildUpon()
            builder.appendQueryParameter(Note.DECK_ID_QUERY_PARAM, deckId.toString())
            return mResolver.bulkInsert(builder.build(), valuesArr)
        }

        override fun findDuplicateNotes(modelId: Long, keys: List<String>): SparseArray<List<NoteInfo>> {
            // Build set of checksums and a HashMap from the key (first field) back to the original index in fieldsArray
            val csums = mutableSetOf<Long>()
            val keyToIndexesMap = mutableMapOf<String, MutableList<Int>>()
            for (i in keys.indices) {
                val key = keys[i]
                csums.add(Utils.fieldChecksum(key))
                keyToIndexesMap.getOrPut(key) { mutableListOf() }.add(i)
            }

            // Query for notes that have specified model and checksum of first field matches
            val sel = String.format(
                Locale.US,
                "%s=%d and %s in (%s)",
                Note.MID,
                modelId,
                Note.CSUM,
                TextUtils.join(",", csums)
            )
            val notesTableCursor = mResolver.query(Note.CONTENT_URI_V2, PROJECTION, sel, null, null)
                ?: return SparseArray()

            // Loop through each note in the cursor, building the result array of duplicates
            val duplicates = SparseArray<List<NoteInfo>>()
            notesTableCursor.use { cursor ->
                while (cursor.moveToNext()) {
                    val note = NoteInfo.buildFromCursor(cursor) ?: continue
                    if (keyToIndexesMap.containsKey(note.key)) {
                        // skip notes that match csum but not key
                        // Add copy of note to EVERY position in duplicates array corresponding to the current key
                        val outputPos = keyToIndexesMap[note.key] ?: continue
                        for (i in outputPos.indices) {
                            addNoteToDuplicatesArray(
                                if (i > 0) NoteInfo(note) else note,
                                duplicates,
                                outputPos[i]
                            )
                        }
                    }
                }
            }
            return duplicates
        }
    }

    companion object {
        const val READ_WRITE_PERMISSION = FlashCardsContract.READ_WRITE_PERMISSION
        const val DEFAULT_DECK_ID = 1L
        private const val TEST_TAG = "PREVIEW_NOTE"
        private const val PROVIDER_SPEC_META_DATA_KEY = "com.ichi2.anki.provider.spec"
        private const val DEFAULT_PROVIDER_SPEC_VALUE = 1 // for when meta-data key does not exist
        private val PROJECTION = arrayOf(Note._ID, Note.FLDS, Note.TAGS)

        /**
         * Get the AnkiDroid package name that the API will communicate with.
         * This can be used to check that a supported version of AnkiDroid is installed,
         * or to get the application label and icon, etc.
         * @param context a Context that can be used to get the PackageManager
         * @return packageId of AnkiDroid if a supported version is installed, otherwise null
         */
        @JvmStatic
        fun getAnkiDroidPackageName(context: Context): String? {
            val manager = context.packageManager
            val pi = manager.resolveContentProvider(FlashCardsContract.AUTHORITY, 0)
            return pi?.packageName
        }
    }
}
