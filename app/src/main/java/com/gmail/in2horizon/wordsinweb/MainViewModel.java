package com.gmail.in2horizon.wordsinweb;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import androidx.room.Room;

import com.gmail.in2horizon.wordsinweb.database.TranslationDao;
import com.gmail.in2horizon.wordsinweb.database.WiwDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private WiwDatabase database;

    private MutableLiveData<TranslationDao> dao = new MediatorLiveData<>();
    private MutableLiveData<String> sourceWord = new MutableLiveData<>();

    private MediatorLiveData<List<String>> liveData = new MediatorLiveData<>();

//    private LiveData<List<String>> liveData = Transformations.switchMap(word,
    //           w -> dao.translate(w));


    MainViewModel() {
        super();
        liveData.addSource(dao,
                d -> {
                    Future<LiveData<List<String>>> f =
                            WiwDatabase.executor.submit(
                                    () -> d.translate(sourceWord.getValue()));
                    try {
                        liveData.setValue(f.get().getValue());
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        liveData.addSource(sourceWord,
                word -> {
                    if ((this.sourceWord.getValue() != null) && (dao.getValue() != null)) {
                      WiwDatabase.executor.execute(()->{
/*
                         List<String> result=
                                 database.getTranslationDao().translate(word.getValue());
                         Log.d(TAG,"result list::"+result);
*/
                            String wildcardWord="%"+word+"%";
                          Log.d(TAG, "test" +word+"::"+ dao.getValue().translate(wildcardWord));

                      });

                        /*  Future<List<String>> f =
                                WiwDatabase.executor.submit(
                                        () -> dao.getValue().translate(word.getValue()));
                        try {
                            Log.d(TAG, "live" + "::" + f.get());
                            liveData.setValue(f.get());
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }*/

                    }

                });
    }

    public void setSourceWord(String src) {
        Log.d(TAG, src + "::" + (dao == null));
        this.sourceWord.setValue(src);
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
        WiwDatabase.executor.execute(() ->
                {
//                    Log.d(TAG, "test" + database.getTranslationDao().translate("oko"));

     /*               Cursor c = database.query(new SimpleSQLiteQuery("select count(*) from " +
                            "Translation"));
                    c.moveToFirst();
                    Log.d(TAG, "count" + c.getString(0));
    */            }

        );
    }


    public LiveData<List<String>> getTranslation() {
        Log.d(TAG, "::" + (dao == null));
        return liveData;

    }

    static final ViewModelInitializer<MainViewModel> initializer = new ViewModelInitializer<>(
            MainViewModel.class,
            creationExtras -> {
                return new MainViewModel();
            }
    );


    @Override
    protected void finalize() throws Throwable {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.finalize();
    }
}
