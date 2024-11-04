package com.example.myapplication;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AbsListView.MultiChoiceModeListener {

    private String[] names = {"One", "Two", "Three", "Four", "Five"};
    private int[] imageIds = {R.drawable.lion, R.drawable.tiger, R.drawable.monkey, R.drawable.dog, R.drawable.cat};
    private ListView listView;
    final List<Map<String, Object>> listItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);


        for (int i = 0; i < names.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("Name", names[i]);
            listItem.put("header", imageIds[i]);
            listItems.add(listItem);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.simple_item, new String[]{"header", "Name"}, new int[]{R.id.header, R.id.name});
        listView = findViewById(R.id.list_view); // 通过listview的id获取ListView
        listView.setAdapter(simpleAdapter); // 加载Adapter
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);//设置了 ListView 的选择模式为 CHOICE_MODE_MULTIPLE_MODAL
        //意味着用户可以通过长按列表项来选择多个项，并且会启动一个 ActionMode（上下文模式），允许用户对选中的项执行操作（如复制、删除等）。
        listView.setMultiChoiceModeListener(this);//这个方法设置了 ListView 的多选模式监听器
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                // 获取所有选中的位置
                SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();
                if (checkedPositions != null) {
                    // 从后往前遍历，避免因删除而改变位置
                    for (int i = checkedPositions.size() - 1; i >= 0; i--) {
                        if (checkedPositions.valueAt(i)) {
                            int position = checkedPositions.keyAt(i);
                            listItems.remove(position);
                        }
                    }
                    // 更新适配器
                    ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
                    // 结束 ActionMode
                    mode.finish();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // 清除选择状态
        listView.clearChoices();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        // 更新ActionMode的标题，显示选中的数量
        int count = listView.getCheckedItemCount();
        mode.setTitle(count > 0 ? +count+"Selected"  : "Select items");
    }
}