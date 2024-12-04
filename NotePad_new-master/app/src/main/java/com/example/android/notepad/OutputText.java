package com.example.android.notepad;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class OutputText extends Activity {
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_CREATE_DATE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
    };
    private EditText mName;
    private Uri mUri;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_text);
        mUri = getIntent().getData();
        mName = (EditText) findViewById(R.id.output_name);

        // 使用LoaderManager来查询数据
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(OutputText.this,
                        mUri,
                        PROJECTION,
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data != null && data.moveToFirst()) {
                    mName.setText(data.getString(1));
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // Do nothing
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (flag) {
            write();
        }
        flag = false;
    }

    public void OutputOk(View v) {
        flag = true;
        finish();
    }

    private void write() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File targetFile = new File(sdCardDir, mName.getText().toString() + ".txt");
                try (PrintWriter ps = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8"))) {
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(mUri, PROJECTION, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        ps.println(cursor.getString(1));
                        ps.println(cursor.getString(2));
                        ps.println("创建时间：" + cursor.getString(3));
                        ps.println("最后一次修改时间：" + cursor.getString(4));
                        cursor.close();
                    }
                }
                Toast.makeText(this, "文件成功保存至" + sdCardDir.getCanonicalPath() + "/" + mName.getText() + ".txt", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}