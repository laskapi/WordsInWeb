package com.gmail.in2horizon.wordsinweb;

import android.content.Context;
import android.util.Log;

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
    private final MutableLiveData<String> searchText= new MutableLiveData<>();

    private final MutableLiveData<TranslationDao> dao = new MutableLiveData<>();
    private final MutableLiveData<String> sourceWord = new MutableLiveData<>();
    private final LiveData<String> translation = Transformations.switchMap(sourceWord,
            word -> {
                String wildcardWord = "%" + word + "%";
                return dao.getValue().translate(wildcardWord);
            });


    public void setSearchText(String searchText) {
        this.searchText.setValue(searchText);
    }
    public LiveData<String> getSearchText(){
        return  searchText;
    }

    public void setSourceWord(String word) {
        this.sourceWord.setValue(word);
    }

    public LiveData<String> getSourceWord() {
        return sourceWord;
    }

    public LiveData<String> getTranslation() {return translation; }

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

    @Override
    protected void finalize() throws Throwable {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.finalize();
    }
}
