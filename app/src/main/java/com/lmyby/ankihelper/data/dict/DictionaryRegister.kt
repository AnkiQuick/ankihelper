package com.lmyby.ankihelper.data.dict

import android.content.Context
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.data.ai.AIConfigRepository

/**
 * Created by liao on 2017/4/27.
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object DictionaryRegister {
    // Register dictionary classes here
    private val classList = arrayOf<Class<*>>(
        Oalde10::class.java,
        Cdepe4::class.java,
        Maldpe::class.java
    )

    fun getDictionaryClassArray(): Array<Class<*>> {
        return classList
    }

    @JvmStatic
    fun getDictionaryObjectList(): List<IDictionary> {
        val dictList = mutableListOf<IDictionary>()

        // Add static dictionary classes
        for (c in classList) {
            try {
                dictList.add(
                    c.getDeclaredConstructor(Context::class.java)
                        .newInstance(MyApplication.getContext()) as IDictionary
                )
            } catch (nsme: NoSuchMethodException) {
                // Handle missing constructor
            } catch (ie: InstantiationException) {
                // Handle instantiation issues
            } catch (ie: IllegalAccessException) {
                // Handle access issues
            } catch (ite: java.lang.reflect.InvocationTargetException) {
                // Handle invocation issues
            }
        }

        // Add AI Dictionary configurations from database
        val aiConfigs = AIConfigRepository.getAllAIDictionaryConfigs()
        for (config in aiConfigs) {
            dictList.add(AIDictionary(config))
        }

        return dictList
    }
}
