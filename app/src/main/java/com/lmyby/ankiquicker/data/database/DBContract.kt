package com.lmyby.ankiquicker.data.database

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object DBContract {
    object History {
        const val TABLE_NAME = "history"
        const val COLUMN_TIME_STAMP = "timestamp"
        const val COLUMN_TYPE = "type"
        const val COLUMN_WORD = "word"
        const val COLUMN_SENTENCE = "sentence"
        const val COLUMN_DICTIONARY = "dictionary"
        const val COLUMN_DEFINITION = "definition"
        const val COLUMN_TRANSLATION = "translation"
        const val COLUMN_NOTE = "note"
        const val COLUMN_TAG = "tag"
    }

    object Plan {
        const val TABLE_NAME = "plan"
        const val COLUMN_PLAN_NAME = "planname"
        const val COLUMN_DICTIONARY_KEY = "dictionarykey"
        const val COLUMN_OUTPUT_DECK_ID = "outputdeckid"
        const val COLUMN_OUTPUT_MODEL_ID = "outputmodelid"
        const val COLUMN_FIELDS_MAP = "fieldsmap"
    }

    object Book {
        const val TABLE_NAME = "book"
        const val COLUMN_ID = "id" // creation EPOCH time in millis
        const val COLUMN_LAST_OPEN_TIME = "lastopentime"
        const val COLUMN_BOOK_NAME = "bookname"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_BOOK_PATH = "bookpath"
        const val COLUMN_READ_POSITION = "readposition" // stored in json format
    }
}
