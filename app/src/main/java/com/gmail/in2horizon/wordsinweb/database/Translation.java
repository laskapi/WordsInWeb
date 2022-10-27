package com.gmail.in2horizon.wordsinweb.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Translation {
    @PrimaryKey(autoGenerate = true)

    long id = 0;
    public String src;
    public String dst;

    public Translation(String src, String dst) {

        this.src = src;
        this.dst = dst;
    }

}