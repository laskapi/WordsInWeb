package com.gmail.in2horizon.wordsinweb;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.gmail.in2horizon.wordsinweb.database.TranslationDao;
import com.gmail.in2horizon.wordsinweb.database.WiwDatabase;

import java.util.List;

public class TransitionsMainViewModel extends ViewModel {

    private static final String TAG = TransitionsMainViewModel.class.getSimpleName();

    private WiwDatabase database;
    private final MutableLiveData<TranslationDao> dao =new MutableLiveData<TranslationDao>();
    private final MutableLiveData<String> sourceWord = new MutableLiveData<String>();
    private final LiveData<List<String>> translation = Transformations.switchMap(sourceWord,
            word -> dao.getValue().translate(word));



    public void setSourceWord(String word) {

        String wildcardWord="%"+word+"%";

        this.sourceWord.setValue(wildcardWord);
    }

    public void setDictionary(Context context, String dictionaryFilename) {

        if (database != null) {
            String actualDatabaseFilename = database.getOpenHelper().getDatabaseName();
            if (actualDatabaseFilename == dictionaryFilename) {
                return;
            }
            database.close();
        }
        database = Room.databaseBuilder(context, WiwDatabase.class, dictionaryFilename).build();
        dao.setValue(database.getTranslationDao());

    }


    public LiveData<List<String>> getTranslation() {
        return translation;
    }

    @Override
    protected void finalize() throws Throwable {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.finalize();
    }
}
