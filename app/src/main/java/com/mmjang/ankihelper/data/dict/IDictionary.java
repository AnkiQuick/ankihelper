package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.widget.ListAdapter;

import java.util.List;

/**
 * Created by liao on 2017/4/13.
 */

public interface IDictionary {
    /**
     * Get a stable, language-independent key for this dictionary
     * This key is used to identify the dictionary in plans and should never change
     * @return A unique, stable identifier for this dictionary
     */
    String getDictionaryKey();

    /**
     * Get the localized display name of this dictionary
     * @return The dictionary name in the current app language
     */
    String getDictionaryName();

    String getIntroduction();

    String[] getExportElementsList();

    List<Definition> wordLookup(String key);

    ListAdapter getAutoCompleteAdapter(Context context, int layout);
}
