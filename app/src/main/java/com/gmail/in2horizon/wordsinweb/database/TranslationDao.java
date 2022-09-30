package com.gmail.in2horizon.wordsinweb.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TranslationDao {

    @Query("SELECT * FROM Translation WHERE en LIKE:en LIMIT 1")
    Translation translate(String en);


    @Insert
    void insertTranslation(Translation translation);

}
