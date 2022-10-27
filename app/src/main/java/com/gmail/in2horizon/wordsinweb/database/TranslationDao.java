package com.gmail.in2horizon.wordsinweb.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TranslationDao {

    @Query("SELECT dst FROM Translation WHERE src LIKE:src LIMIT 1")
    String translate(String src);


    @Insert
    void insertTranslation(Translation translation);
    @Insert
    void insertAll(List<Translation> list);
}
