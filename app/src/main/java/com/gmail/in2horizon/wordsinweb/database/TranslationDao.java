package com.gmail.in2horizon.wordsinweb.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TranslationDao {

    @Query("SELECT dst FROM Translation WHERE src LIKE :source LIMIT 1 ")
    LiveData<String> translate(String source);


    @Insert
    void insertTranslation(Translation translation);
    @Insert
    void insertAll(List<Translation> list);
}
