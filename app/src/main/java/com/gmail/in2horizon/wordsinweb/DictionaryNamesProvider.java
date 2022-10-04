package com.gmail.in2horizon.wordsinweb;

public class DictionaryNamesProvider {

    private static DictionaryNamesProvider instance;
    private DictionaryNamesProvider() {
    }

    DictionaryNamesProvider getInstance() {
        if (instance == null) {
            instance = new DictionaryNamesProvider();
        }
        return instance;
    }


void loadDictionaries(){

}
}
