package com.mmjang.ankihelper.util;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.mmjang.ankihelper.util.RegexUtil.isKorean;

/**
 * Created by liao on 2017/5/4.
 */

public class TextSplitter {

    private static final String DEVIDER = "__DEVIDER___DEVIDER__";
    private static final String TAG = "TextSplitter";
    private static final int MAX_TEXT_LENGTH = 20000; // Increased limit for better multi-line text support

    @NonNull
    public static List<String> getLocalSegments(String str) {
        Log.d(TAG, "getLocalSegments called with text length: " + (str != null ? str.length() : 0));
        
        // Limit text length to prevent performance issues
        if (str != null && str.length() > MAX_TEXT_LENGTH) {
            Log.w(TAG, "Text is very long (" + str.length() + " chars), truncating to " + MAX_TEXT_LENGTH + " chars");
            // Find the last complete sentence or word boundary to avoid cutting in middle of word
            str = str.substring(0, MAX_TEXT_LENGTH);
            int lastSentenceEnd = Math.max(str.lastIndexOf('.'), Math.max(str.lastIndexOf('!'), str.lastIndexOf('?')));
            int lastWordBoundary = str.lastIndexOf(' ');
            int cutPoint = Math.max(lastSentenceEnd, lastWordBoundary);
            if (cutPoint > MAX_TEXT_LENGTH * 0.9) { // Only cut if we can preserve most of the text
                str = str.substring(0, cutPoint + 1);
            }
        }
        
        str = preProcess(str);
        List<String> txts = new ArrayList<String>();
        String s = "";
        for (int i = 0; i < str.length(); i++) {
            char first = str.charAt(i);
            //当到达末尾的时候
            if (i + 1 >= str.length()) {
                s = s + first;
                break;
            }
            char next = str.charAt(i + 1);
            if ((RegexUtil.isChinese(first) && !RegexUtil.isChinese(next)) || (!RegexUtil.isChinese(first) && RegexUtil.isChinese(next)) ||
                    (Character.isLetter(first) && !Character.isLetter(next)) || (Character.isDigit(first) && !Character.isDigit(next)) ||
                    (isKorean(first) && !isKorean(next)) || (!isKorean(first) && isKorean(next))

                    ) {
                s = s + first + DEVIDER;
            } else if (RegexUtil.isSymbol(first) || StringUtil.isSpace(first) ||
                    first == Constant.LEFT_BOLD_SUBSTITUDE.charAt(0) ||
                    first == Constant.RIGHT_BOLD_SUBSTITUDE.charAt(0)
                    ) {
                s = s + DEVIDER + first + DEVIDER;
            } else {
                s = s + first;
            }
        }
        str = s;
        str = str.replace("\n", DEVIDER + "\n" + DEVIDER);
        String[] texts = str.split(DEVIDER);
        Log.d(TAG, "Split into " + texts.length + " parts");
        
        for (String text : texts) {
            if (text.equals(DEVIDER))
                continue;

            if (RegexUtil.isEnglish(text)) {
                txts.add(text);
                continue;
            }

            if (RegexUtil.isSpecialWord(text)) {
                txts.add(text);
                continue;
            }

            if (RegexUtil.isNumber(text)) {
                txts.add(text);
                continue;
            }
            for (int i = 0; i < text.length(); i++) {
                txts.add(text.charAt(i) + "");
            }
        }
        
        Log.d(TAG, "getLocalSegments returning " + txts.size() + " segments");
        return txts;
    }

    private static String preProcess(String str) {
        Log.d(TAG, "preProcess called with text length: " + (str != null ? str.length() : 0));
        if (str == null) return "";
        str = str.replace("<b>", Constant.LEFT_BOLD_SUBSTITUDE)
                .replace("</b>", Constant.RIGHT_BOLD_SUBSTITUDE);
        if (!str.contains("<br/>") && !str.contains("<br>")) {
            return str;
        } else {
            //html mode
            String html = str.replace("\n", "")
                    .replace("<br/><br/>", "<br/>")
                    .replace("<br><br>", "<br/>")
                    .replace("<br/>", "\n")
                    .replace("<br>", "\n");
            return html;
        }
    }

}