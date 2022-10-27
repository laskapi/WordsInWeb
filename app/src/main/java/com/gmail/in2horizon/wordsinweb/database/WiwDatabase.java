package com.gmail.in2horizon.wordsinweb.database;

import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(version = 1, entities = {Translation.class},exportSchema = true)
public abstract class WiwDatabase extends RoomDatabase {

    static final ExecutorService executor =
            Executors.newSingleThreadExecutor();


    public abstract TranslationDao getTranslationDao();
}
