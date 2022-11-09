package com.gmail.in2horizon.wordsinweb;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;

import com.gmail.in2horizon.wordsinweb.database.TranslationDao;
import com.gmail.in2horizon.wordsinweb.database.WiwDatabase;
import com.gmail.in2horizon.wordsinweb.dictionarymanager.DictionaryManager;

public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private WiwDatabase database;
    private final MutableLiveData<String> searchText= new MutableLiveData<>();

    private final MutableLiveData<TranslationDao> dao = new MutableLiveData<>();
    private final MutableLiveData<String> sourceWord = new MutableLiveData<>();
    private final LiveData<String> translation = Transformations.switchMap(sourceWord,
            word -> {
                String wildcardWord = "%" + word + "%";
                return dao.getValue().translate(wildcardWord);
            });

    private final MutableLiveData<DictionaryManager> manager= new MutableLiveData<>();

    public MutableLiveData<DictionaryManager> getManager() {
        return manager;
    }
    public void setManager(DictionaryManager manager){
        this.manager.setValue(manager);
    }


    public void setSearchText(String searchText) {
        this.searchText.setValue(searchText);
    }

    public LiveData<String> getSearchText(){
        return  searchText;
    }

    public void setSourceWord(String word) {
        this.sourceWord.setValue(word);
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

    public boolean isDaoReady() {
        return dao.getValue()!=null;
    }



    @Override
    protected void finalize() throws Throwable {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.finalize();
    }
}
