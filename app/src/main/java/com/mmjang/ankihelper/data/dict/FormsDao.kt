package com.mmjang.ankihelper.data.dict

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FormsDao {
    @Query("SELECT bases FROM forms WHERE hwd = :query")
    suspend fun getForms(query: String): String?
}
