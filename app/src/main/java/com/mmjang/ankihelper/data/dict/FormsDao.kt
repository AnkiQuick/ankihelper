package com.mmjang.ankihelper.data.dict

import androidx.room.Dao
import androidx.room.Query
import androidx.room.SkipQueryVerification

@Dao
interface FormsDao {
    @SkipQueryVerification
    @Query("SELECT bases FROM forms WHERE hwd = :query")
    fun getForms(query: String): String?
}
