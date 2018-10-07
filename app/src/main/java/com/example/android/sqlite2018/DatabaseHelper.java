package com.example.android.sqlite2018;

import android.app.NotificationChannel;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

@Database(version = 1, entities = {Note.class}, exportSchema = false)
public abstract class DatabaseHelper extends RoomDatabase {
    abstract DaoNote getDao();
}
