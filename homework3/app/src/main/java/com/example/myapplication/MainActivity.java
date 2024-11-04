    package com.example.myapplication;

    import androidx.appcompat.app.AppCompatActivity;

    import android.app.AlertDialog;
    import android.os.Bundle;
    import android.text.Layout;
    import android.view.View;
    import android.widget.ListView;
    import android.widget.SimpleAdapter;
    import android.widget.TableLayout;
    import android.widget.Toast;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class MainActivity extends AppCompatActivity {
        //准备数据
      private String[] names={"Lion","Tiger","Monkey","Dog","Cat","Elephant"};
      private int[] imageIds={R.drawable.lion, R.drawable.tiger,R.drawable.monkey,R.drawable.dog,
              R.drawable.cat,R.drawable.elephant
      };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
           setContentView(R.layout.listview);//确定要显示的listview位置
          //  setContentView(R.layout.activity_main);
          List<Map<String,Object>> listItems=new ArrayList<>();//创建资源表列
            for (int i = 0; i < names.length ; i++) {//遍历上面的数组将"取数据名(可任取)",数组[i]放入Map<String,Object>,再加入List<Map<>>,
                Map<String,Object> listItem =new HashMap<>();
                listItem.put("Name",names[i]);
                listItem.put("header",imageIds[i]);
                listItems.add(listItem);
            }
            //创建simpleAdapter,五个参数(容器,资源列表,每一列的格式item,要显示的数据名,item中要加载这些数据的id)4,5的顺序是意义对应的
            SimpleAdapter simpleAdapter=new SimpleAdapter(this,listItems,
                    R.layout.simple_item, new String[] {"header","Name"},new int[]{R.id.header,R.id.name});
        ListView list1=findViewById(R.id.list1);//通过listview的id获取Listview
        list1.setAdapter(simpleAdapter);//加载Adapter

list1.setOnItemClickListener((parent, view, position, id)->{//单监听事件 单机弹出选择的名字
    Toast toast=Toast.makeText(getApplicationContext(), names[position], Toast.LENGTH_SHORT);
    toast.show();
});


    }
    }