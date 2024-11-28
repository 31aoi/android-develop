# NotePad

## 一 功能扩展如下：

1.增加时间戳显示

2.添加笔记查询功能

3.笔记导出

4.笔记排序

## 二 主界面增加时间戳：

效果:



![img_1.png](img_1.png)

修改Note2:



![img_2.png](img_2.png)

1.在NoteList类的PROJECTION中定义显示时间戳:

```
private static final String[] PROJECTION = new String[] {
        NotePad.Notes._ID, // 0
        NotePad.Notes.COLUMN_NAME_TITLE, // 1
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//显示时间戳

};
```



2.在NoteList类的dataColumns，viewIDs中加入时间戳的相关部分:时间戳的数据和viewId:

```
private String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,
        NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;
private int[] viewIDs = { android.R.id.text1 , R.id.time };
```



3.在NotePadeProvider中insert方法对日期进行格式转换:

```
//获取当前系统时间
Long nowtime = Long.valueOf(System.currentTimeMillis());
Date date = new Date(nowtime);
SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
String Time = format.format(date);

// 如果值映射中不包含创建日期，则将值设置为当前时间。
if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, Time);
}

// 如果值映射中不包含修改日期，则将值设置为当前时间。
if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, Time);
}
```



4.在NodeEditor的updateNote方法中转换日期格式:

```
Long nowTime = Long.valueOf(System.currentTimeMillis());
java.sql.Date date = new Date(nowTime);
SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
String Time = format.format(date);
// Sets up a map to contain values to be updated in the provider.
ContentValues values = new ContentValues();
values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, Time);
```



5.添加list布局中时间戳的textview

```
<TextView
    android:id="@+id/time"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?android:attr/textAppearance"
    android:paddingLeft="5dip"
    android:textColor="@color/black"/>
```



## 三、笔记搜索：

效果:

![img_3.png](img_3.png)
![img_5.png](img_5.png)
![img_6.png](img_6.png)

1.添加菜单文件中的搜索图标:

```
<item
    android:id="@+id/menu_search"
    android:title="@string/menu_search"
    android:icon="@android:drawable/ic_search_category_default"
    android:showAsAction="always">
</item>
```



2.在NoteLIst类的onOptionsItemSelected方法中添加case情况:

```
case R.id.menu_search:
    Intent intent = new Intent();
    intent.setClass(NotesList.this,NoteSearch.class);
    NotesList.this.startActivity(intent);
    return true;
```

3.新建布局文件note_search_list.xml:新建搜素框和列表的view

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"> <!-- 设置整个布局的背景颜色为白色 -->

    <SearchView//搜索框
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:background="#7A99D7"
        android:queryHint="搜索"
        android:layout_alignParentTop="true">
    </SearchView>

    <ListView
        android:id="@android:id/list"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/white"> <!-- 设置ListView的背景颜色为白色 -->
    </ListView>

</LinearLayout>
```

4.新建NoteSearch类:

```
package com.example.android.notepad;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

public class NoteSearch extends ListActivity implements SearchView.OnQueryTextListener {
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE
    };
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search_list);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        updateNotesList(newText);
        return true;
    }

    private void updateNotesList(String newText) {
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { "%" + newText + "%" };//newText为搜索的内容
        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
        setListAdapter(new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                new String[]{NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE},
                new int[]{android.R.id.text1, R.id.time}
        ));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
```

5.在mainifests注册NoteSearch:

```
<activity
    android:name="NoteSearch"
    android:label="@string/title_notes_search">
    </activity>
```

## 四、笔记导出：
![img_7.png](img_7.png)

![img_8.png](img_8.png)

![img_9.png](img_9.png)

1.添加导出按钮

```
<item android:id="@+id/menu_output"
    android:title="导出" />
```

2.创建导出方法用于从NoteEditor`的Activity中启动OutputText`的Activity

```
private final void outputNote() {
Intent intent = new Intent(null,mUri);
intent.setClass(NoteEditor.this,OutputText.class);
NoteEditor.this.startActivity(intent);
}
```

3.创建导出界面的布局:

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/output_name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="8dp"
        android:ems="10"
        android:hint="@string/enter_name"
        android:inputType="textCapSentences"
        android:maxLength="50" />

    <Button
        android:id="@+id/output_ok"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/confirm"
        android:onClick="onConfirmClick" />
</LinearLayout>
```

4.新建OutputText类:

```
import android.app.Activity;
    import android.database.Cursor;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
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
    private Cursor mCursor;
    private EditText mName;
    private Uri mUri;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.output_text);
    mUri = getIntent().getData();
    mCursor = managedQuery(
    mUri,
    PROJECTION,
    null,
    null,
    null
    );
    mName = (EditText) findViewById(R.id.output_name);
    }

    @Override
    protected void onResume() {
    super.onResume();
    if (mCursor != null && !mCursor.isClosed()) {
    mCursor.moveToFirst();
    mName.setText(mCursor.getString(1));
    }
    }

    @Override
    protected void onPause() {
    super.onPause();
    if (mCursor != null && !mCursor.isClosed() && flag) {
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
    File targetFile = new File(sdCardDir.getCanonicalPath() + "/" + mName.getText().toString() + ".txt");
    PrintWriter ps = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8"));
    ps.println(mCursor.getString(1));
    ps.println(mCursor.getString(2));
    ps.println("创建时间：" + mCursor.getString(3));
    ps.println("最后一次修改时间：" + mCursor.getString(4));
    ps.close();
    Toast.makeText(this, "保存成功,保存位置：" + sdCardDir.getCanonicalPath() + "/" + mName.getText() + ".txt", Toast.LENGTH_LONG).show();
    }
    } catch (Exception e) {
    e.printStackTrace();
    }
    }
    }
```

5.授权:

```xml
<!-- 向SD卡写入数据权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<!-- 读取SD卡数据权限 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

## 五 、笔记排序：

按创建时间排序:

![img_10.png](img_10.png)

按修改时间排序:

![img_11.png](img_11.png)

1.添加菜单选项:

```
<item
    android:id="@+id/menu_sort1"
    android:title="按创建时间排序"/>
<item
    android:id="@+id/menu_sort2"
    android:title="按修改时间排序"/>
```

2.在notesList中添加菜单case:

```
//创建时间排序
case R.id.menu_sort1:
     cursor = managedQuery(
            getIntent().getData(),
            PROJECTION,
            null,
            null,
            NotePad.Notes._ID
    );
     adapter = new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item,
            cursor,
            dataColumns,
            viewIDs
    );
    setListAdapter(adapter);
    return true;
//修改时间排序
case R.id.menu_sort2:
    cursor = managedQuery(
            getIntent().getData(),
            PROJECTION,
            null,
            null,
            NotePad.Notes.DEFAULT_SORT_ORDER
    );
    adapter = new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item,
            cursor,
            dataColumns,
            viewIDs
    );
    setListAdapter(adapter);
    return true;
```

## 六、UI美化：更换背景
![img_12.png](img_12.png)
![img_13.png](img_13.png)
![img_14.png](img_14.png)

1.添加菜单选项:

```
<item
    android:id="@+id/menu_change_background"
    android:title="更换背景颜色"
    android:showAsAction="never" />
```

2.在list中添加菜单case:

```
case R.id.menu_change_background:
    changeBackgroundColor();
    return true;
```

3.添加changeBackgroundColor方法:

```
private void changeBackgroundColor() {
    Random random = new Random();
    int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    getWindow().getDecorView().setBackgroundColor(color);
}
```