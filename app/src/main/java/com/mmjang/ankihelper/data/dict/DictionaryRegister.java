package com.mmjang.ankihelper.data.dict;

import android.content.Context;

import com.mmjang.ankihelper.MyApplication;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liao on 2017/4/27.
 */

public class DictionaryRegister {
    //在这里注册词典类
    private static Class[] classList = new Class[]{
            Ode2.class,
            Collins.class,
            CollinsEnEn.class,
            EudicSentence.class,
            SolrDictionary.class,
            RenRenCiDianSentence.class,
            Dub91Sentence.class,
            Esdict.class,
            Frdict.class,
            Dedict.class,
            Cloze.class,
            JiSho.class,
            VocabCom.class,
            Mnemonic.class,
            WebsterLearners.class,
            HujiangJapanese.class,
            Handian.class,
            BingOxford.class,
            BingImage.class,
            DictionaryDotCom.class,
            UrbanDict.class,
            IdiomDict.class,
            Oalde10.class,
            Cdepe4.class,
            Maldpe.class
    };

    private static List<IDictionary> dictList;

    public Class[] getDictionaryClassArray() {
        return classList;
    }

    public static List<IDictionary> getDictionaryObjectList() {
        //if (dictList == null) {
            dictList = new ArrayList<>();
            for (Class c : classList) {
                try {
                    dictList.add(
                            (IDictionary) c.getConstructor(Context.class).newInstance(MyApplication.getContext())
                    );
                } catch (NoSuchMethodException nsme) {
                } catch (InstantiationException ie) {
                } catch (IllegalAccessException ie) {
                } catch (InvocationTargetException ite) {
                }
            }
        //}
        return dictList;
    }
}
