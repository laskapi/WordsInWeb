package com.gmail.in2horizon.wordsinweb.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Translation {
    @PrimaryKey
    @NonNull
    Long id=0l;
    public String en;
    public String de;
    public String fr;
    public String es;
    public String pl;
}