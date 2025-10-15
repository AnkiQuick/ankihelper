package com.lmyby.ankihelper.data.dict;

import android.content.Context;

import com.lmyby.ankihelper.MyApplication;
import com.lmyby.ankihelper.data.ai.AIDictionaryConfig;
import com.lmyby.ankihelper.data.ai.AIConfigRepository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liao on 2017/4/27.
 */

public class DictionaryRegister {
    //在这里注册词典类
    private static Class<?>[] classList = new Class<?>[]{
            Oalde10.class,
            Cdepe4.class,
            Maldpe.class
    };

    private static List<IDictionary> dictList;

    public Class[] getDictionaryClassArray() {
        return classList;
    }

    public static List<IDictionary> getDictionaryObjectList() {
        dictList = new ArrayList<>();
        
        // Add static dictionary classes
        for (Class<?> c : classList) {
            try {
                dictList.add(
                        (IDictionary) c.getDeclaredConstructor(Context.class).newInstance(MyApplication.getContext())
                );
            } catch (NoSuchMethodException nsme) {
                // Handle missing constructor
            } catch (InstantiationException ie) {
                // Handle instantiation issues
            } catch (IllegalAccessException ie) {
                // Handle access issues
            } catch (InvocationTargetException ite) {
                // Handle invocation issues
            }
        }
        
        // Add AI Dictionary configurations from database
        List<AIDictionaryConfig> aiConfigs = AIConfigRepository.getAllAIDictionaryConfigs();
        for (AIDictionaryConfig config : aiConfigs) {
            dictList.add(new AIDictionary(config));
        }
        
        return dictList;
    }
}
