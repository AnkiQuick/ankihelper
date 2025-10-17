package com.lmyby.ankiquicker.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.data.book.Book
import com.lmyby.ankiquicker.data.history.HistoryPOJO
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object DatabaseManager {
    private const val TB_DICT = "dict"
    private const val TB_ENTRY = "entry"
    private const val CL_ID = "id"
    private const val CL_NAME = "name"
    private const val CL_LANG = "lang"
    private const val CL_ELEMENTS = "elements"
    private const val CL_TMPL = "tmpl"
    private const val CL_DESCRIPTION = "description"
    private const val CL_DICT_ID = "dict_id"
    private const val CL_HEADWORD = "headword"
    private const val CL_ENTRY_TEXTS = "entry_texts"
    private const val SPLITTER = "\t" //original file is splitted by \t, so it's safe.
    private const val SQL_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS headword_index ON entry (headword)"
    private const val SQL_DROP_INDEX = "DROP INDEX IF EXISTS headword_index"
    private const val SQL_CHECK_DICT_TABLE = "SELECT name FROM sqlite_master WHERE type='table' AND name='dict'"
    private const val SQL_CHECK_ENTRY_TABLE = "SELECT name FROM sqlite_master WHERE type='table' AND name='entry'"

    private lateinit var mContext: Context
    lateinit var mDatabase: SQLiteDatabase
        private set

    @JvmStatic
    fun getInstance(): DatabaseManager {
        if (!::mDatabase.isInitialized) {
            mContext = MyApplication.getContext()
            Log.d("DatabaseManager", "Creating DatabaseHelper instance")
            val dbHelper = DatabaseHelper(mContext)
            Log.d("DatabaseManager", "Getting writable database")
            mDatabase = dbHelper.writableDatabase
            Log.d("DatabaseManager", "Database path: ${mDatabase.path}")
            Log.d("DatabaseManager", "Database initialized successfully")
        }
        return this
    }

    @JvmStatic
    fun clearDB() {
        mDatabase.delete(TB_DICT, null, null)
        mDatabase.delete(TB_ENTRY, null, null)
    }

    @JvmStatic
    fun getHeadwordColumnName(): String = CL_HEADWORD

    @JvmStatic
    fun checkDictTableExists(): Boolean {
        return try {
            Log.d("DatabaseManager", "Checking if dict table exists")
            mDatabase.rawQuery(SQL_CHECK_DICT_TABLE, null).use { cursor ->
                val exists = cursor.count > 0
                Log.d("DatabaseManager", "Dict table exists check result: $exists")
                exists
            }
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error checking dict table: ${e.message}")
            false
        }
    }

    @JvmStatic
    fun checkEntryTableExists(): Boolean {
        return try {
            Log.d("DatabaseManager", "Checking if entry table exists")
            mDatabase.rawQuery(SQL_CHECK_ENTRY_TABLE, null).use { cursor ->
                val exists = cursor.count > 0
                Log.d("DatabaseManager", "Entry table exists check result: $exists")
                exists
            }
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error checking entry table: ${e.message}")
            false
        }
    }

    @JvmStatic
    fun initializeDictTable() {
        Log.d("DatabaseManager", "Initializing dict table")
        val exists = checkDictTableExists()
        Log.d("DatabaseManager", "Dict table exists: $exists")
        if (!exists) {
            Log.d("DatabaseManager", "Creating dict table")
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS $TB_DICT " +
                    "(id integer, name text, lang text, elements text, description text, tmpl text)")
            mDatabase.execSQL("CREATE INDEX IF NOT EXISTS headword_index ON entry (headword)")
        }
    }

    @JvmStatic
    fun initializeEntryTable() {
        Log.d("DatabaseManager", "Initializing entry table")
        val exists = checkEntryTableExists()
        Log.d("DatabaseManager", "Entry table exists: $exists")
        if (!exists) {
            Log.d("DatabaseManager", "Creating entry table")
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS $TB_ENTRY " +
                    "(dict_id integer, headword text, entry_texts text)")
            mDatabase.execSQL("CREATE INDEX IF NOT EXISTS headword_index ON entry (headword)")
        }
    }

    @JvmStatic
    fun addDictionaryInformation(id: Int, name: String, lang: String, elements: Array<String>, description: String, tmpl: String) {
        val values = ContentValues().apply {
            put(CL_ID, id)
            put(CL_NAME, name)
            put(CL_LANG, lang)
            put(CL_ELEMENTS, joinFields(elements))
            put(CL_DESCRIPTION, description)
            put(CL_TMPL, tmpl)
        }
        mDatabase.insert(TB_DICT, null, values)
    }

    @JvmStatic
    fun addEntries(dictId: Int, entries: List<Array<String>>) { //assume first column is headword
        mDatabase.beginTransaction()
        try {
            for (entry in entries) {
                if (entry.size < 2) {
                    continue
                }
                val values = ContentValues().apply {
                    put(CL_DICT_ID, dictId)
                    put(CL_HEADWORD, entry[0].lowercase())   //must have at least 2 columns, enforced in manager
                    put(CL_ENTRY_TEXTS, joinFields(entry))
                }
                mDatabase.insert(TB_ENTRY, null, values)
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    @JvmStatic
    fun getWordLookupCursor(dictId: Int, word: String): Cursor {
        return mDatabase.query(
            TB_ENTRY,
            arrayOf(CL_HEADWORD, CL_ENTRY_TEXTS),
            "$CL_DICT_ID= ? AND $CL_HEADWORD= ? COLLATE NOCASE",
            arrayOf(dictId.toString(), word),
            null, null, null, "50"
        )
    }

    @JvmStatic
    fun getFilterCursor(dictId: Int, query: String): Cursor {
        return mDatabase.query(
            TB_ENTRY,
            arrayOf("rowid _id", CL_HEADWORD),
            "$CL_DICT_ID=? AND $CL_HEADWORD LIKE ?",
            arrayOf(dictId.toString(), "$query%"),
            CL_HEADWORD,
            null,
            null
        )
    }

    @JvmStatic
    fun getDictIdList(): List<Int> {
        return mDatabase.query(
            TB_DICT,
            arrayOf(CL_ID),
            "",
            null,
            null,
            null,
            null
        ).use { cursor ->
            val result = mutableListOf<Int>()
            while (cursor.moveToNext()) {
                result.add(cursor.getInt(0))
            }
            result
        }
    }

    @JvmStatic
    fun dropHwdIndex() {
        mDatabase.execSQL(SQL_DROP_INDEX)
    }

    @JvmStatic
    fun createHwdIndex() {
        mDatabase.execSQL(SQL_CREATE_INDEX)
    }

    @JvmStatic
    fun queryHeadword(dictId: Int, q: String): List<Array<String>> {
        val result = mutableListOf<Array<String>>()
        if (q.isEmpty()) {
            return result
        }
        val query = q.lowercase()
        mDatabase.query(
            TB_ENTRY,
            arrayOf(CL_ENTRY_TEXTS),
            "$CL_DICT_ID=? AND $CL_HEADWORD=?",
            arrayOf(dictId.toString(), query),
            null, null, null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(fromFieldsString(cursor.getString(0)))
            }
        }
        return result
    }

    @JvmStatic
    fun insertHistory(history: HistoryPOJO): Boolean {
        val values = ContentValues().apply {
            put(DBContract.History.COLUMN_TIME_STAMP, history.timeStamp)
            put(DBContract.History.COLUMN_DEFINITION, history.definition)
            put(DBContract.History.COLUMN_DICTIONARY, history.dictionary)
            put(DBContract.History.COLUMN_NOTE, history.note)
            put(DBContract.History.COLUMN_TAG, history.tag)
            put(DBContract.History.COLUMN_SENTENCE, history.sentence)
            put(DBContract.History.COLUMN_TYPE, history.type)
            put(DBContract.History.COLUMN_WORD, history.word)
        }
        val result = mDatabase.insert(DBContract.History.TABLE_NAME, null, values)
        return result >= 0
    }

    @JvmStatic
    fun insertManyHistory(historyPOJOS: List<HistoryPOJO>) {
        mDatabase.beginTransaction()
        try {
            for (history in historyPOJOS) {
                insertHistory(history)
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    @JvmStatic
    fun getHistoryAfter(timeStamp: Long): List<HistoryPOJO> {
        val result = mutableListOf<HistoryPOJO>()
        mDatabase.rawQuery(
            "select ${DBContract.History.COLUMN_TIME_STAMP}, ${DBContract.History.COLUMN_DEFINITION}, " +
                    "${DBContract.History.COLUMN_DICTIONARY}, ${DBContract.History.COLUMN_NOTE}, " +
                    "${DBContract.History.COLUMN_TAG}, ${DBContract.History.COLUMN_SENTENCE}, " +
                    "${DBContract.History.COLUMN_TYPE}, ${DBContract.History.COLUMN_WORD} " +
                    "from ${DBContract.History.TABLE_NAME} where ${DBContract.History.COLUMN_TIME_STAMP} > $timeStamp",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val historyPOJO = HistoryPOJO()
                historyPOJO.timeStamp = cursor.getLong(0)
                historyPOJO.definition = cursor.getString(1)
                historyPOJO.dictionary = cursor.getString(2)
                historyPOJO.note = cursor.getString(3)
                historyPOJO.tag = cursor.getString(4)
                historyPOJO.sentence = cursor.getString(5)
                historyPOJO.type = cursor.getInt(6)
                historyPOJO.word = cursor.getString(7)
                result.add(historyPOJO)
            }
        }
        return result
    }

    @JvmStatic
    fun getAllPlan(): List<OutputPlanPOJO> {
        val result = mutableListOf<OutputPlanPOJO>()
        mDatabase.rawQuery(
            "select ${DBContract.Plan.COLUMN_PLAN_NAME}, ${DBContract.Plan.COLUMN_DICTIONARY_KEY}, " +
                    "${DBContract.Plan.COLUMN_OUTPUT_DECK_ID}, ${DBContract.Plan.COLUMN_OUTPUT_MODEL_ID}, " +
                    "${DBContract.Plan.COLUMN_FIELDS_MAP} from ${DBContract.Plan.TABLE_NAME}",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val outputPlanPOJO = OutputPlanPOJO()
                outputPlanPOJO.planName = cursor.getString(0)
                outputPlanPOJO.dictionaryKey = cursor.getString(1)
                outputPlanPOJO.outputDeckId = cursor.getLong(2)
                outputPlanPOJO.outputModelId = cursor.getLong(3)
                outputPlanPOJO.setFieldsMapString(cursor.getString(4))
                result.add(outputPlanPOJO)
            }
        }
        return result
    }

    @JvmStatic
    fun getPlanByName(planName: String): OutputPlanPOJO? {
        var result: OutputPlanPOJO? = null
        mDatabase.rawQuery(
            "select ${DBContract.Plan.COLUMN_PLAN_NAME}, ${DBContract.Plan.COLUMN_DICTIONARY_KEY}, " +
                    "${DBContract.Plan.COLUMN_OUTPUT_DECK_ID}, ${DBContract.Plan.COLUMN_OUTPUT_MODEL_ID}, " +
                    "${DBContract.Plan.COLUMN_FIELDS_MAP} from ${DBContract.Plan.TABLE_NAME} " +
                    "where ${DBContract.Plan.COLUMN_PLAN_NAME}='$planName'",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result = OutputPlanPOJO()
                result.planName = cursor.getString(0)
                result.dictionaryKey = cursor.getString(1)
                result.outputDeckId = cursor.getLong(2)
                result.outputModelId = cursor.getLong(3)
                result.setFieldsMapString(cursor.getString(4))
            }
        }
        return result
    }

    @JvmStatic
    fun refreshPlanWith(outputPlanPOJOS: List<OutputPlanPOJO>) {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBContract.Plan.TABLE_NAME, null, null)
            for (outputPlanPOJO in outputPlanPOJOS) {
                insertPlan(outputPlanPOJO)
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    @JvmStatic
    fun deletePlanByName(planName: String): Int {
        return mDatabase.delete(
            DBContract.Plan.TABLE_NAME,
            "${DBContract.Plan.COLUMN_PLAN_NAME}='$planName'",
            null
        )
    }

    //the plan name don't change
    @JvmStatic
    fun updatePlan(outputPlanPOJO: OutputPlanPOJO): Int {
        return updatePlan(outputPlanPOJO, outputPlanPOJO.planName)
    }

    //if we want to change the plan's name
    @JvmStatic
    fun updatePlan(outputPlanPOJO: OutputPlanPOJO, oldPlanName: String): Int {
        val contentValues = ContentValues().apply {
            put(DBContract.Plan.COLUMN_PLAN_NAME, outputPlanPOJO.planName)
            put(DBContract.Plan.COLUMN_DICTIONARY_KEY, outputPlanPOJO.dictionaryKey)
            put(DBContract.Plan.COLUMN_OUTPUT_DECK_ID, outputPlanPOJO.outputDeckId)
            put(DBContract.Plan.COLUMN_OUTPUT_MODEL_ID, outputPlanPOJO.outputModelId)
            put(DBContract.Plan.COLUMN_FIELDS_MAP, outputPlanPOJO.getFieldsMapString())
        }
        return mDatabase.update(
            DBContract.Plan.TABLE_NAME, contentValues,
            "${DBContract.Plan.COLUMN_PLAN_NAME}='$oldPlanName'", null
        )
    }

    @JvmStatic
    fun insertPlan(outputPlanPOJO: OutputPlanPOJO): Long {
        val contentValues = ContentValues().apply {
            put(DBContract.Plan.COLUMN_PLAN_NAME, outputPlanPOJO.planName)
            put(DBContract.Plan.COLUMN_DICTIONARY_KEY, outputPlanPOJO.dictionaryKey)
            put(DBContract.Plan.COLUMN_OUTPUT_DECK_ID, outputPlanPOJO.outputDeckId)
            put(DBContract.Plan.COLUMN_OUTPUT_MODEL_ID, outputPlanPOJO.outputModelId)
            put(DBContract.Plan.COLUMN_FIELDS_MAP, outputPlanPOJO.getFieldsMapString())
        }
        return mDatabase.insert(DBContract.Plan.TABLE_NAME, null, contentValues)
    }

    private fun joinFields(fields: Array<String>): String {
        return fields.joinToString(SPLITTER) + SPLITTER
    }

    private fun fromFieldsString(fieldsString: String): Array<String> {
        return fieldsString.split(SPLITTER).toTypedArray()
    }

    //book operations
    @JvmStatic
    fun insertBook(book: Book): Long {
        return mDatabase.insert(DBContract.Book.TABLE_NAME, null, book.getContentValues())
    }

    @JvmStatic
    fun updateBook(book: Book): Int {
        return mDatabase.update(DBContract.Book.TABLE_NAME, book.getContentValues(), "id=${book.id}", null)
    }

    @JvmStatic
    fun deleteBook(book: Book): Int {
        return mDatabase.delete(DBContract.Book.TABLE_NAME, "id=${book.id}", null)
    }

    @JvmStatic
    fun getLastBooks(): List<Book> {
        val bookList = mutableListOf<Book>()
        mDatabase.query(
            DBContract.Book.TABLE_NAME,
            arrayOf(
                DBContract.Book.COLUMN_ID, DBContract.Book.COLUMN_LAST_OPEN_TIME,
                DBContract.Book.COLUMN_BOOK_NAME, DBContract.Book.COLUMN_AUTHOR,
                DBContract.Book.COLUMN_BOOK_PATH, DBContract.Book.COLUMN_READ_POSITION
            ),
            null, null, null, null, "${DBContract.Book.COLUMN_LAST_OPEN_TIME} desc"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                bookList.add(
                    Book(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                    )
                )
            }
        }
        return bookList
    }

    @JvmStatic
    fun refreshBook(oldbook: Book): Book? {
        mDatabase.query(
            DBContract.Book.TABLE_NAME,
            arrayOf(
                DBContract.Book.COLUMN_ID, DBContract.Book.COLUMN_LAST_OPEN_TIME,
                DBContract.Book.COLUMN_BOOK_NAME, DBContract.Book.COLUMN_AUTHOR,
                DBContract.Book.COLUMN_BOOK_PATH, DBContract.Book.COLUMN_READ_POSITION
            ),
            "id=${oldbook.id}", null, null, null, null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Book(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                )
            } else {
                null
            }
        }
    }
}
