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

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * 这个活动允许用户编辑笔记的标题。它显示一个包含EditText的浮动窗口。
 *
 * 注意：请注意，这个活动中的提供者操作是在UI线程上进行的。
 * 这不是一个好的实践。这里只是为了使代码更易读。一个真正的
 * 应用程序应该使用{@link android.content.AsyncQueryHandler}
 * 或{@link android.os.AsyncTask}对象来在单独的线程上异步执行操作。
 */
public class TitleEditor extends Activity {

    /**
     * 这是一个特殊的意图动作，意味着“编辑笔记的标题”。
     */
    public static final String EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE";

    // 创建一个投影，返回笔记ID和笔记内容。
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
    };

    // 在提供者返回的Cursor中标题列的位置。
    private static final int COLUMN_INDEX_TITLE = 1;

    // 一个Cursor对象，将包含查询提供者以获取笔记的结果。
    private Cursor mCursor;

    // 一个EditText对象，用于保存编辑的标题。
    private EditText mText;

    // 一个URI对象，用于正在编辑标题的笔记。
    private Uri mUri;

    /**
     * 当Android首次启动活动时，这个方法被调用。从传入的
     * Intent中，它确定所需的编辑类型，然后执行它。
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置这个活动对象的UI的视图。
        setContentView(R.layout.title_editor);

        // 获取激活此活动的Intent，并从中获取我们需要编辑标题的笔记的URI。
        mUri = getIntent().getData();

        /*
         * 使用随触发Intent一起传递的URI，获取笔记。
         *
         * 注意：这正在UI线程上完成。它将阻塞线程，直到查询完成。在样本应用中，与基于本地数据库的简单提供者相比，
         * 阻塞将是短暂的，但在真正的应用中，你应该使用
         * android.content.AsyncQueryHandler或android.os.AsyncTask。
         */

        mCursor = managedQuery(
                mUri,        // 要检索的笔记的URI。
                PROJECTION,  // 要检索的列
                null,        // 没有使用选择标准，所以不需要where列。
                null,        // 没有使用where列，所以不需要where值。
                null         // 不需要排序顺序。
        );

        // 获取EditText框的视图ID
        mText = (EditText) this.findViewById(R.id.title);
    }

    /**
     * 当活动即将进入前台时，这个方法被调用。这发生在
     * 活动到达任务栈顶部时，或者当它首次启动时。
     *
     * 显示所选笔记的当前标题。
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 验证在onCreate()中进行的查询是否有效。如果有效，则
        // Cursor对象不是null。如果它是*空的*，那么mCursor.getCount() == 0。
        if (mCursor != null) {

            // 刚刚检索到的Cursor，所以它的索引设置在检索到的第一个记录*之前*。这将其移动到第一个记录。
            mCursor.moveToFirst();

            // 在EditText对象中显示当前标题文本。
            mText.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }

    /**
     * 当活动失去焦点时，这个方法被调用。
     *
     * 对于编辑信息的活动对象，onPause()可能是保存更改的地方。Android应用程序模型基于
     * “保存”和“退出”不是必需的操作的理念。当用户从活动中导航离开时，他们不应该必须回到
     * 它来完成他们的工作。离开的行为应该保存所有内容，并将活动留在一个状态，在这个状态下Android可以销毁它，如果必要的话。
     *
     * 使用文本框中当前的文本更新笔记。
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 验证在onCreate()中进行的查询是否有效。如果有效，则
        // Cursor对象不是null。如果它是*空的*，那么mCursor.getCount() == 0。

        if (mCursor != null) {

            // 创建一个值映射，用于更新提供者。
            ContentValues values = new ContentValues();

            // 在值映射中，将标题设置为编辑框的当前内容。
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, mText.getText().toString());

            /*
             * 使用笔记的新标题更新提供者。
             *
             * 注意：这正在UI线程上完成。它将阻塞线程，直到
             * 更新完成。在样本应用中，与基于本地数据库的简单提供者相比，
             * 阻塞将是短暂的，但在真正的应用中，你应该使用
             * android.content.AsyncQueryHandler或android.os.AsyncTask。
             */
            getContentResolver().update(
                    mUri,    // 要更新的笔记的URI。
                    values,  // 包含要更新的列和要使用的值的值映射。
                    null,    // 没有使用选择标准，所以不需要“where”列。
                    null     // 没有使用“where”列，所以不需要“where”值。
            );

        }
    }

    public void onClickOk(View v) {
        finish();
    }
}