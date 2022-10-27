package com.gmail.in2horizon.wordsinweb.dictionarymanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import androidx.room.Room;

import com.gmail.in2horizon.wordsinweb.database.Translation;
import com.gmail.in2horizon.wordsinweb.database.TranslationDao;
import com.gmail.in2horizon.wordsinweb.database.WiwDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomDbBuilder {
    private static final String TAG = RoomDbBuilder.class.getSimpleName();
    private static final String TMP_FILENAME = "wordsinweb.sqlite3";
    private static final int WORK_IN_PROGRESS = 0;
    private static final int WORK_DONE = 1;


    private final Context context;


    RoomDbBuilder(Context context) {
        this.context = context;
    }


    void build(String srcUrl, Handler handler) {
        ExecutorService ex = Executors.newSingleThreadExecutor();


        ex.execute(() -> {
            String tmpUrl = context.getFilesDir() + "/" + TMP_FILENAME;
            Uri u = Uri.parse(srcUrl);
            File f = new File("" + u);
            String filename = f.getName();

            WiwDatabase translationsDB = Room.databaseBuilder(context,
                    WiwDatabase.class, filename)
                    .build();
            TranslationDao translationsDao = translationsDB.getTranslationDao();

            try {

                InputStream inputStream =
                        new URL(srcUrl).openStream();
                FileOutputStream fileOutputStream = new FileOutputStream(tmpUrl);

                byte[] buffer = new byte[65536];
                int n = 0;
                int percent = 0;
                while (-1 != (n = inputStream.read(buffer))) {
                    if (!handler.hasMessages(WORK_IN_PROGRESS) && percent < 79) {
                        percent += 1;
                        Message msg = Message.obtain(handler, WORK_IN_PROGRESS, percent + " %");
                        msg.sendToTarget();
                    }

                    fileOutputStream.write(buffer, 0, n);
                }
                fileOutputStream.flush();
                inputStream.close();
                fileOutputStream.close();

                SQLiteDatabase sqLiteDatabase =
                        SQLiteDatabase.openDatabase(tmpUrl, null,
                                SQLiteDatabase.OPEN_READONLY);

                String query = "SELECT written_rep,trans_list FROM simple_translation";
                Cursor cursor = sqLiteDatabase.rawQuery(query, null);
                List<Translation> list = new ArrayList<Translation>();
                int counter = 0;
                int progressDivider = cursor.getCount() / 10;
                int progressStep = (99 - percent) / 10;
                if (cursor.moveToFirst()) {
                    do {
                        counter++;
                        list.add(new Translation(cursor.getString(0), cursor.getString(1)));
                        if (counter == progressDivider) {
                            translationsDao.insertAll(list);
                            list.clear();
                            counter = 0;
                            if (!handler.hasMessages(WORK_IN_PROGRESS) && percent < 99) {
                                percent += progressStep;
                                Message msg = Message.obtain(handler, WORK_IN_PROGRESS, percent + " %");
                                msg.sendToTarget();

                            }

                        }
                    } while (cursor.moveToNext());
                    translationsDB.close();
                    handler.removeMessages(WORK_IN_PROGRESS);
                    Message msg = Message.obtain(handler, WORK_DONE, null);
                    msg.sendToTarget();

                }

                cursor.close();
                translationsDB.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    boolean deleteDb(String database) {
        return context.deleteDatabase(database);
    }

}
