/*
 * 版权所有 (C) 2007 The Android Open Source Project
 *
 * 根据Apache License, Version 2.0（“许可证”）授权；
 * 除非遵守许可证，否则不得使用此文件。
 * 你可以在以下网址获得许可证的副本：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非适用法律要求或书面同意，否则按照许可证分发的软件是基于“原样”分发的，
 * 没有任何明示或暗示的保证或条件。具体的许可证语言管理权限和限制。
 */

package com.example.android.notepad;


import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * 提供对笔记数据库的访问。每个笔记都有标题、内容、创建日期和修改日期。
 */
public class NotePadProvider extends ContentProvider implements PipeDataWriter<Cursor> {
    // 用于调试和日志记录
    private static final String TAG = "NotePadProvider";

    /**
     * 提供者使用的基础数据存储数据库
     */
    private static final String DATABASE_NAME = "note_pad.db";

    /**
     * 数据库版本
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * 用于从数据库选择列的投影映射
     */
    private static HashMap<String, String> sNotesProjectionMap;

    /**
     * 用于从数据库选择列的投影映射
     */
    private static HashMap<String, String> sLiveFolderProjectionMap;

    /**
     * 标准投影，用于普通笔记的有趣列。
     */
    private static final String[] READ_NOTE_PROJECTION = new String[] {
            NotePad.Notes._ID,               // 投影位置0，笔记的id
            NotePad.Notes.COLUMN_NAME_NOTE,  // 投影位置1，笔记的内容
            NotePad.Notes.COLUMN_NAME_TITLE, // 投影位置2，笔记的标题
    };
    private static final int READ_NOTE_NOTE_INDEX = 1;
    private static final int READ_NOTE_TITLE_INDEX = 2;

    /*
     * 用于Uri匹配器的常量，根据传入URI的模式选择一个动作
     */
    // 传入的URI与笔记URI模式匹配
    private static final int NOTES = 1;

    // 传入的URI与笔记ID URI模式匹配
    private static final int NOTE_ID = 2;

    // 传入的URI与Live Folder URI模式匹配
    private static final int LIVE_FOLDER_NOTES = 3;

    /**
     * Uri匹配器实例
     */
    private static final UriMatcher sUriMatcher;

    // 新的DatabaseHelper的句柄。
    private DatabaseHelper mOpenHelper;

    /**
     * 一个块，实例化并设置静态对象
     */
    static {

        /*
         * 创建并初始化URI匹配器
         */
        // 创建一个新实例
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 添加一个模式，将URIs终止于"notes"路由到NOTES操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);

        // 添加一个模式，将URIs终止于"notes"加上一个整数
        // 路由到笔记ID操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        // 添加一个模式，将URIs终止于live_folders/notes路由到一个
        // live folder操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        /*
         * 创建并初始化一个投影映射，返回所有列
         */

        // 创建一个新的投影映射实例。映射返回一个列名
        // 给定一个字符串。这两个通常相等。
        sNotesProjectionMap = new HashMap<String, String>();

        // 将"_ID"映射到列名"_ID"
        sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);

        // 将"title"映射到"title"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_TITLE);

        // 将"note"映射到"note"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);

        // 将"created"映射到"created"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE,
                NotePad.Notes.COLUMN_NAME_CREATE_DATE);

        // 将"modified"映射到"modified"
        sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);

        /*
         * 创建并初始化一个投影映射，用于处理Live Folders
         */

        // 创建一个新的投影映射实例
        sLiveFolderProjectionMap = new HashMap<String, String>();

        // 将"_ID"映射到"_ID AS _ID"用于live folder
        sLiveFolderProjectionMap.put(LiveFolders._ID, NotePad.Notes._ID + " AS " + LiveFolders._ID);

        // 将"NAME"映射到"title AS NAME"
        sLiveFolderProjectionMap.put(LiveFolders.NAME, NotePad.Notes.COLUMN_NAME_TITLE + " AS " +
                LiveFolders.NAME);
    }

    /**
     *
     * 这个类帮助打开、创建和升级数据库文件。为了测试目的，设置为包可见性。
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            // 调用超构造函数，请求默认的光标工厂。
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         *
         * 创建底层数据库，表名和列名来自NotePad类。
         */
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER" //数据库增加color属性
                    + ");");
        }


        /**
         *
         * 演示提供者必须考虑当底层数据存储改变时会发生什么。在这个示例中，数据库是通过销毁现有数据来升级数据库的。
         * 一个真正的应用程序应该在原地升级数据库。
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // 日志记录数据库正在升级
            Log.w(TAG, "正在升级数据库从版本 " + oldVersion + " 到 "
                    + newVersion + "，这将销毁所有旧数据");

            // 杀死表和现有数据
            db.execSQL("DROP TABLE IF EXISTS notes");

            // 重新创建数据库的新版本
            onCreate(db);
        }
    }

    /**
     *
     * 通过创建一个新的DatabaseHelper来初始化提供者。onCreate()在Android创建提供者以响应客户端的解析器请求时自动调用。
     */
    @Override
    public boolean onCreate() {

        // 创建一个新的帮助对象。注意，数据库本身在尝试访问它之前不会打开，
        // 并且只有在它尚不存在时才会创建。
        mOpenHelper = new DatabaseHelper(getContext());

        // 假设任何失败都会由抛出的异常报告。
        return true;
    }

    /**
     * 当客户端调用{@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}。
     * 查询数据库并返回包含结果的光标。
     *
     * @return 包含查询结果的光标。如果查询没有结果或发生异常，光标存在但为空。
     * @throws IllegalArgumentException 如果传入的URI模式无效。
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // 构建一个新的查询构建器并设置其表名
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NotePad.Notes.TABLE_NAME);

        /**
         * 根据URI模式匹配选择投影并调整“where”子句。
         */
        switch (sUriMatcher.match(uri)) {
            // 如果传入的URI是笔记，选择笔记投影
            case NOTES:
                qb.setProjectionMap(sNotesProjectionMap);
                break;

            /* 如果传入的URI是单个笔记ID，选择笔记ID投影，并附加“_ID = <noteID>”到where子句，以便
             * 它选择那个单一笔记
             */
            case NOTE_ID:
                qb.setProjectionMap(sNotesProjectionMap);
                qb.appendWhere(
                        NotePad.Notes._ID +    // ID列的名称
                                "=" +
                                // 笔记ID本身在传入URI中的位置
                                uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION));
                break;

            case LIVE_FOLDER_NOTES:
                // 如果传入的URI来自live folder，选择live folder投影。
                qb.setProjectionMap(sLiveFolderProjectionMap);
                break;

            default:
                // 如果URI不匹配任何已知模式，抛出异常。
                throw new IllegalArgumentException("未知URI " + uri);
        }



        String orderBy;
        // 如果没有指定排序顺序，使用默认值
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        } else {
            // 否则，使用传入的排序顺序
            orderBy = sortOrder;
        }

        // 以“读”模式打开数据库对象，因为不需要执行写操作。
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        /*
         * 执行查询。如果没有尝试读取数据库时出现问题，那么返回一个Cursor对象；否则，光标变量包含null。如果没有记录被选中，
         * 那么Cursor对象为空，Cursor.getCount()返回0。
         */
        Cursor c = qb.query(
                db,            // 要查询的数据库
                projection,    // 要从查询中返回的列
                selection,     // where子句的列
                selectionArgs, // where子句的值
                null,          // 不对行进行分组
                null,          // 不按行组过滤
                orderBy        // 排序顺序
        );

        // 告诉光标要监视的URI，这样它就知道其源数据何时改变
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * 这个方法在客户端调用{@link android.content.ContentResolver#getType(Uri)}时被调用。
     * 返回给定参数URI的MIME数据类型。
     *
     * @param uri 需要MIME类型的URI。
     * @return URI的MIME类型。
     * @throws IllegalArgumentException 如果传入的URI模式无效。
     */
    @Override
    public String getType(Uri uri) {

        /**
         * 根据传入的URI模式选择MIME类型
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是笔记或live folders，返回一般内容类型。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return NotePad.Notes.CONTENT_TYPE;

            // 如果模式是笔记ID，返回笔记ID内容类型。
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;

            // 如果URI模式不匹配任何允许的模式，抛出异常。
            default:
                throw new IllegalArgumentException("未知URI " + uri);
        }
    }

//BEGIN_INCLUDE(stream)
    /**
     * 描述打开笔记URI作为流时支持的MIME类型。
     */
    static ClipDescription NOTE_STREAM_TYPES = new ClipDescription(null,
            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });

    /**
     * 返回可用数据流的类型。支持特定笔记的URI。应用程序可以将这样的笔记转换为纯文本流。
     *
     * @param uri 要分析的URI
     * @param mimeTypeFilter 要检查的MIME类型。此方法仅返回与过滤器匹配的数据流MIME类型。
     * @return 数据流MIME类型。目前，仅返回text/plain。
     * @throws IllegalArgumentException 如果URI模式不匹配任何支持的模式。
     */
    @Override
    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        /**
         * 根据传入的URI模式选择数据流类型。
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是笔记或live folders，返回null。不支持这种类型的URI的数据流。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return null;

            // 如果模式是笔记ID，并且MIME过滤器是text/plain，则返回text/plain
            case NOTE_ID:
                return NOTE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);

            // 如果URI模式不匹配任何允许的模式，抛出异常。
            default:
                throw new IllegalArgumentException("未知URI " + uri);
        }
    }

    /**
     * 返回每种支持的数据流类型的流。此方法对传入的URI执行查询，然后使用
     * {@link android.content.ContentProvider#openPipeHelper(Uri, String, Bundle, Object,
     * PipeDataWriter)} 在另一个线程中将数据转换为流。
     *
     * @param uri 指向数据流的URI模式
     * @param mimeTypeFilter 包含MIME类型的字符串。此方法尝试获取这种MIME类型的数据流。
     * @param opts 调用者提供的额外选项。内容提供者可以按需解释。
     * @return AssetFileDescriptor 文件的句柄。
     * @throws FileNotFoundException 如果没有与传入URI关联的文件。
     */
    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts)
            throws FileNotFoundException {

        // 检查MIME类型过滤器是否匹配支持的MIME类型。
        String[] mimeTypes = getStreamTypes(uri, mimeTypeFilter);

        // 如果MIME类型受支持
        if (mimeTypes != null) {

            // 为此URI检索笔记。使用此提供者定义的查询方法，
            // 而不是使用数据库查询方法。
            Cursor c = query(
                    uri,                    // 笔记的URI
                    READ_NOTE_PROJECTION,   // 获取包含笔记ID、标题和内容的投影
                    null,                   // 没有WHERE子句，获取所有匹配记录
                    null,                   // 由于没有WHERE子句，没有选择条件
                    null                    // 使用默认排序顺序（修改日期，降序）
            );

            // 如果查询失败或光标为空，则停止
            if (c == null || !c.moveToFirst()) {

                // 如果光标为空，只需关闭光标并返回
                if (c != null) {
                    c.close();
                }

                // 如果光标为null，则抛出异常
                throw new FileNotFoundException("无法查询 " + uri);
            }

            // 启动一个新线程，将流数据传回给调用者。
            return new AssetFileDescriptor(
                    openPipeHelper(uri, mimeTypes[0], opts, c, this), 0,
                    AssetFileDescriptor.UNKNOWN_LENGTH);
        }

        // 如果MIME类型不受支持，返回对文件的只读句柄。
        return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    /**
     * 实现{@link android.content.ContentProvider.PipeDataWriter}
     * 将光标中的数据转换为客户端可以读取的流数据的实际工作。
     */
    @Override
    public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
                                Bundle opts, Cursor c) {
        // 我们目前只支持从单个笔记条目转换为文本，
        // 因此这里不需要检查光标数据类型。
        FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            pw.println(c.getString(READ_NOTE_TITLE_INDEX));
            pw.println("");
            pw.println(c.getString(READ_NOTE_NOTE_INDEX));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Ooops", e);
        } finally {
            c.close();
            if (pw != null) {
                pw.flush();
            }
            try {
                fout.close();
            } catch (IOException e) {
            }
        }
    }
//END_INCLUDE(stream)

    /**
     * 当客户端调用{@link android.content.ContentResolver#insert(Uri, ContentValues)}时调用。
     * 在数据库中插入新行。此方法为任何不在传入映射中包含的列设置默认值。
     * 如果插入了行，则通知监听器数据已更改。
     * @return 插入行的行ID。
     * @throws SQLException 如果插入失败。
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {


        // 验证传入的URI。仅允许完整的提供者URI用于插入。
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("未知URI " + uri);
        }

        // 用于保存新记录值的映射。
        ContentValues values;

        // 如果传入的值映射不为空，则使用它作为新值。
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // 否则，创建一个新的值映射
            values = new ContentValues();
        }


        String Time = TimeUtil.getCurrentTimeFormatted();

        // 如果值映射中不包含创建日期，则将值设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, Time);
        }

        // 如果值映射中不包含修改日期，则将值设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, Time);
        }

        // 如果值映射中不包含标题，则将值设置为默认标题。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
        }

        // 如果值映射中不包含笔记文本，则将值设置为空字符串。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
        }

        // 以“写”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // 执行插入并返回新笔记的ID。
        long rowId = db.insert(
                NotePad.Notes.TABLE_NAME,        // 要插入的表
                NotePad.Notes.COLUMN_NAME_NOTE,  // 一个技巧，如果values为空，则SQLite将此列的值设置为null

                values                           // 列名和要插入到列中的值的映射

        );

        // 如果插入成功，行ID存在。
        if (rowId > 0) {
            // 使用笔记ID模式创建一个URI，并附加新的行ID。
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, rowId);

            // 通知注册在此提供者上的监听器数据已更改。
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // 如果插入未成功，则行ID <= 0。抛出异常。
        throw new SQLException("无法插入行到 " + uri);
    }

    /**
     * 当客户端调用{@link android.content.ContentResolver#delete(Uri, String, String[])}时调用。
     * 从数据库中删除记录。如果传入的URI匹配笔记ID URI模式，则此方法删除URI中ID指定的记录。否则，它删除一组记录。
     * 记录或记录集还必须匹配输入的选择标准where和whereArgs。
     * 如果删除了行，则通知监听器变化。
     * @return 如果使用了“where”子句，返回受影响的行数，否则返回0。要删除所有行并获得行计数，请使用“1”作为where子句。
     * @throws IllegalArgumentException 如果传入的URI模式无效。
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        // 以“写”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

// 根据传入的URI模式执行删除操作。
        switch (sUriMatcher.match(uri)) {

            // 如果传入的模式与笔记的一般模式匹配，根据传入的“where”列和参数执行删除。
            case NOTES:
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名
                        where,                     // 传入的where子句列名
                        whereArgs                  // 传入的where子句值
                );
                break;

            // 如果传入的URI匹配单个笔记ID，根据传入的数据执行删除，但修改where子句以限制特定的笔记ID。
            case NOTE_ID:
                /*
                 * 开始构建最终的WHERE子句，将其限制为
                 * 所需的笔记ID。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID列名
                                " = " +                                          // 测试相等
                                uri.getPathSegments()                            // 传入的笔记ID
                                        .get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果有额外的选择标准，将它们附加到最终的WHERE子句
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行删除。
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名。
                        finalWhere,                // 最终的WHERE子句
                        whereArgs                  // 传入的where子句值。
                );
                break;

            // 如果传入的模式无效，抛出异常。
            default:
                throw new IllegalArgumentException("未知URI " + uri);
        }

        /* 获取当前上下文的内容解析器对象的句柄，并通知它
         * 传入的URI已更改。该对象将此传递给解析器框架，
         * 并通知已注册为该提供者的观察者。
         */
        getContext().getContentResolver().notifyChange(uri, null);

// 返回删除的行数。
        return count;
    }

    /**
     * 当客户端调用
     * {@link android.content.ContentResolver#update(Uri,ContentValues,String,String[])}
     * 时调用。更新数据库中的记录。values映射中的键指定的列名将被更新为映射中的新数据。
     * 如果传入的URI匹配笔记ID URI模式，则该方法更新URI中ID指定的一条记录；否则，它更新一组记录。
     * 记录或记录集必须匹配由where和whereArgs指定的选择标准。
     * 如果行被更新，则通知监听器变化。
     *
     * @param uri 要匹配和更新的URI模式。
     * @param values 列名（键）和新值（值）的映射。
     * @param where 一个SQL "WHERE"子句，根据列值选择记录。如果此值为null，则选择所有匹配URI模式的记录。
     * @param whereArgs 选择标准数组。如果"where"参数包含值占位符（"?"），那么每个占位符被数组中对应的元素替换。
     * @return 更新的行数。
     * @throws IllegalArgumentException 如果传入的URI模式无效。
     */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        // 以“写”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // 根据传入的URI模式执行更新
        switch (sUriMatcher.match(uri)) {

            // 如果传入的URI匹配一般笔记模式，则根据传入的数据执行更新。
            case NOTES:
                // 执行更新并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名。
                        values,                   // 列名和新值的映射。
                        where,                    // where子句列名。
                        whereArgs                 // where子句列值。
                );
                break;
// 如果传入的URI匹配单个笔记ID，则根据传入的数据执行更新，但修改where子句以限制特定的笔记ID。
            case NOTE_ID:
                // 从传入的URI中获取笔记ID
                String noteId = uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION);

                /*
                 * 开始构建最终的WHERE子句，将其限制为传入的
                 * 笔记ID。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID列名
                                " = " +                                          // 测试相等
                                uri.getPathSegments()                            // 传入的笔记ID
                                        .get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果有额外的选择标准，将它们附加到最终的WHERE子句
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行更新并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名。
                        values,                   // 列名和新值的映射。
                        finalWhere,               // 最终的WHERE子句
                        whereArgs                 // where子句列值，或者如果值在where参数中，则为null。
                );
                break;
            // 如果传入的模式无效，抛出异常。
            default:
                throw new IllegalArgumentException("未知URI " + uri);
        }

        /* 获取当前上下文的内容解析器对象的句柄，并通知它
         * 传入的URI已更改。该对象将此传递给解析器框架，
         * 并通知已注册为该提供者的观察者。
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // 返回更新的行数。
        return count;
    }

    /**
     * 测试包可以调用此方法以获得NotePadProvider底层数据库的句柄，
     * 以便它可以将测试数据插入数据库。测试用例类负责在测试上下文中实例化提供者；
     * {@link android.test.ProviderTestCase2} 在调用setUp()期间执行此操作。
     *
     * @return 提供者数据的数据库助手对象的句柄。
     */
    DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper;
    }
}
