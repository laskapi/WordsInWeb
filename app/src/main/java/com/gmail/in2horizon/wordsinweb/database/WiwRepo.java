package com.gmail.in2horizon.wordsinweb.database;

import android.content.Context;

import androidx.room.Room;

public class WiwRepo {

    private static final String TAG = WiwRepo.class.getSimpleName();
    private static volatile WiwRepo instance;

    private WiwDatabase database;
    private TranslationDao wiwDao;

    public static WiwRepo getInstance(Context context, String dictionaryFilename) {
        if (instance == null) {
            synchronized (WiwRepo.class) {
                if (instance == null) {
                    instance = new WiwRepo();
                }
            }
        }
        instance.init(context, dictionaryFilename);
        return instance;
    }

    private WiwRepo() {
    }

    public String translate(String src) {
     database.getQueryExecutor().execute(()->wiwDao.translate(src));

    }

    private void init(Context context, String dictionaryFilename) {

        if (database != null) {
            String actualDatabaseFilename = database.getOpenHelper().getDatabaseName();
            if (actualDatabaseFilename == dictionaryFilename) {
                return;
            }
            database.close();
        }
        database = Room.databaseBuilder(context, WiwDatabase.class, dictionaryFilename).build();
        wiwDao = database.getTranslationDao();


    }

    @Override
    protected void finalize() throws Throwable {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.finalize();
    }
}
