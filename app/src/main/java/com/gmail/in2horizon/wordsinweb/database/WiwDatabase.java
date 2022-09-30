package com.gmail.in2horizon.wordsinweb.database;

import androidx.room.RoomDatabase;

@androidx.room.Database(version = 1, entities = {Translation.class},exportSchema = true)
public abstract class WiwDatabase extends RoomDatabase {
    public abstract TranslationDao translationDao();
}
