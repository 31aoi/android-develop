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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 定义了记事本内容提供者与其客户端之间的契约。契约定义了客户端需要访问提供者的一个或多个数据表的信息。
 * 契约是一个公共的、不可扩展的（final）类，其中包含定义列名和URI的常量。
 * 一个编写良好的客户端仅依赖于契约中的常量。
 */
public final class NotePad {

    public static final String AUTHORITY = "com.google.provider.NotePad";

    // 此类不能被实例化
    private NotePad() {
    }

    /**
     * 笔记表契约
     */
    public static final class Notes implements BaseColumns {
        public static final String COLUMN_NAME_BACK_COLOR = "color";
        public static final int DEFAULT_COLOR = 0;
        public static final int YELLOW_COLOR = 1;
        public static final int BLUE_COLOR = 2;
        public static final int GREEN_COLOR = 3;
        public static final int RED_COLOR = 4;

        // 此类不能被实例化
        private Notes() {}

        /**
         * 此提供者提供的数据表名称
         */
        public static final String TABLE_NAME = "notes";



        /*
         * URI定义
         */

        /**
         * 此提供者的URI方案部分
         */
        private static final String SCHEME = "content://";

        /**
         * URI的路径部分
         */

        /**
         * 笔记URI的路径部分
         */
        private static final String PATH_NOTES = "/notes";

        /**
         * 笔记ID URI的路径部分
         */
        private static final String PATH_NOTE_ID = "/notes/";

        /**
         * 在笔记ID URI的路径部分中笔记ID段的0相对位置
         */
        public static final int NOTE_ID_PATH_POSITION = 1;

        /**
         * 活页夹URI的路径部分
         */
        private static final String PATH_LIVE_FOLDER = "/live_folders/notes";

        /**
         * 此表的content://风格的URL
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

        /**
         * 单个笔记的内容URI基础。调用者必须
         * 在这个Uri后追加一个数字笔记id来检索一个笔记
         */
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);

        /**
         * 单个笔记的内容URI匹配模式，通过其ID指定。使用这个来匹配
         * 传入的URIs或者构造一个Intent。
         */
        public static final Uri CONTENT_ID_URI_PATTERN
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        /**
         * 活页夹中笔记列表的内容Uri模式
         */
        public static final Uri LIVE_FOLDER_URI
                = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);

        /*
         * MIME类型定义
         */

        /**
         * {@link #CONTENT_URI}提供的笔记目录的MIME类型。
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * 单个笔记的{@link #CONTENT_URI}子目录的MIME类型。
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * 此表的默认排序顺序
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /*
         * 列定义
         */

        /**
         * 笔记标题的列名
         * <P>类型：TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * 笔记内容的列名
         * <P>类型：TEXT</P>
         */
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * 创建时间戳的列名
         * <P>类型：INTEGER （来自System.curentTimeMillis()的长整型）</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * 修改时间戳的列名
         * <P>类型：INTEGER （来自System.curentTimeMillis()的长整型）</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}