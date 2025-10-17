package com.lmyby.ankiquicker.domain

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.lmyby.ankiquicker.data.Settings
import java.util.Locale

/**
 * Manager for audio playback and text-to-speech
 * Converted to Kotlin as part of Phase 3 domain logic migration
 * Modernized to use AudioAttributes in Phase 14.4
 */
object PlayAudioManager {
    private const val TAG = "PlayAudioManager"
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null

    private fun playAudio(context: Context, url: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener { mp ->
                    mp.start()
                }
            }
        }

        mediaPlayer?.apply {
            // Use modern AudioAttributes API (API 21+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            setDataSource(context, Uri.parse(url))
            prepareAsync()

            setOnCompletionListener { mp ->
                mp.reset()
            }

            setOnErrorListener { mp, _, _ ->
                mp.reset()
                false
            }
        }
    }

    private fun killMediaPlayer() {
        mediaPlayer?.let {
            try {
                it.reset()
                it.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun playPronounceVoice(context: Context, word: String) {
        val lastPronounceLanguage = Settings.getInstance(context).getLastPronounceLanguage()
        val youdaoLanguageType = PronounceManager.getYoudaoTypeFromLanguageIndex(lastPronounceLanguage)
        try {
            // URL encode the word to handle special characters and spaces
            val encodedWord = Uri.encode(word)
            playAudio(context, "http://dict.youdao.com/dictvoice?audio=$encodedWord&le=$youdaoLanguageType")
        } catch (e: Exception) {
            Toast.makeText(context, "获取发音失败,请检查网络设置或单词拼写。", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to play pronunciation for word: $word", e)
        }
    }

    const val EN_PRONOUNCE_BRITISH = 1
    const val EN_PRONOUNCE_AMERICAN = 2

    /**
     * 播放英文发音
     *
     * @param word
     * @param voiceType 1 英音 2 美音
     */
    @JvmStatic
    fun playEngPronounceVoice(context: Context, word: String, voiceType: Int) {
        try {
            // URL encode the word to handle special characters and spaces
            val encodedWord = Uri.encode(word)
            playAudio(context, "http://dict.youdao.com/dictvoice?audio=$encodedWord&type=$voiceType")
        } catch (e: Exception) {
            Toast.makeText(context, "获取发音失败,请检查网络设置或单词拼写。", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to play English pronunciation for word: $word", e)
        }
    }

    /**
     * 播放中文语音
     *
     * @param context
     * @param chinese
     */
    @JvmStatic
    fun playCNPronVoice(context: Context, chinese: String) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {
                        // No action needed on start
                    }

                    override fun onDone(utteranceId: String) {
                        killTTS()
                    }

                    override fun onError(utteranceId: String) {
                        Toast.makeText(context, "发音失败", Toast.LENGTH_SHORT).show()
                        killTTS()
                    }
                })

                val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE) ?: TextToSpeech.LANG_NOT_SUPPORTED
                when (result) {
                    TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                        Log.e("error", "This Language is not supported")
                    }
                    else -> {
                        convertTextToSpeech(context, chinese)
                    }
                }
            } else {
                Log.e("error", "Initialization Failed!")
            }
        }
    }

    private fun killTTS() {
        tts?.let {
            it.stop()
            it.shutdown()
            tts = null
        }
    }

    private fun convertTextToSpeech(context: Context, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(context, text)
        } else {
            ttsUnder20(text)
        }
    }

    @Suppress("DEPRECATION")
    private fun ttsUnder20(text: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(context: Context, text: String) {
        val utteranceId = context.hashCode().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
}
