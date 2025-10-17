/***************************************************************************************
 *                                                                                      *
 * Copyright (c) 2016 Timothy Rae <perceptualchaos2@gmail.com>                          *
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

import android.database.Cursor
import com.ichi2.anki.FlashCardsContract

/**
 * Representation of the contents of a note in AnkiDroid.
 * Converted to Kotlin as part of complete Java-to-Kotlin migration
 */
class NoteInfo private constructor(
    private val mId: Long,
    private val mFields: Array<String>,
    private val mTags: Set<String>
) {

    /**
     * Clone a NoteInfo object
     * @param parent the object to clone
     */
    constructor(parent: NoteInfo) : this(
        parent.id,
        parent.fields.clone(),
        parent.tags.toSet()
    )

    /** Note ID */
    val id: Long
        get() = mId

    /** The array of fields */
    val fields: Array<String>
        get() = mFields

    /** The set of tags */
    val tags: Set<String>
        get() = mTags

    /** The first field **/
    val key: String
        get() = fields[0]

    companion object {
        /**
         * Static initializer method to build a NoteInfo object from a Cursor
         * @param cursor from a query to FlashCardsContract.Note.CONTENT_URI
         * @return a NoteInfo object or null if the cursor was not valid
         */
        internal fun buildFromCursor(cursor: Cursor): NoteInfo? {
            return try {
                val idIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID)
                val fldsIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS)
                val tagsIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.TAGS)
                val fields = Utils.splitFields(cursor.getString(fldsIndex))
                val id = cursor.getLong(idIndex)
                val tags = Utils.splitTags(cursor.getString(tagsIndex))?.toSet() ?: emptySet()
                NoteInfo(id, fields ?: emptyArray(), tags)
            } catch (e: Exception) {
                null
            }
        }
    }
}
