package com.gmail.in2horizon.wordsinweb.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TranslationDao {

    @Query("SELECT * FROM Translation WHERE src LIKE:src LIMIT 1")
    Translation translate(String src);


    @Insert
    void insertTranslation(Translation translation);
    @Insert
    void insertAll(List<Translation> list);
}
