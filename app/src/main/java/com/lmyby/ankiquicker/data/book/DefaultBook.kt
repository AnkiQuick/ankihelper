package com.lmyby.ankiquicker.data.book

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object DefaultBook {
    @JvmStatic
    fun getDefaultBook(): List<Book> {
        val book1 = Book(
            id = System.currentTimeMillis(),
            lastOpenTime = System.currentTimeMillis(),
            bookName = "The Graveyard Book",
            author = "Neil Gaiman",
            bookPath = "file:///android_asset/book/grave.epub",
            readPosition = ""
        )

        val book2 = Book(
            id = System.currentTimeMillis(),
            lastOpenTime = System.currentTimeMillis(),
            bookName = "Moon over Manifest",
            author = "Clare Vanderpool",
            bookPath = "file:///android_asset/book/moon.epub",
            readPosition = ""
        )

        return listOf(book1)
        // return listOf(book1, book2)
    }
}
