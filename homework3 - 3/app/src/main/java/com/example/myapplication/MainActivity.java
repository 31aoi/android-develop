    package com.example.myapplication;

    import androidx.appcompat.app.AppCompatActivity;

    import android.graphics.Color;
    import android.os.Bundle;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;
    import android.view.SubMenu;
    import android.widget.TextView;
    import android.widget.Toast;


    public class MainActivity extends AppCompatActivity {

        private static final int FONT_10=0x111;
        private static final int FONT_12=0x112;
        private static final int FONT_14=0x113;
        private static final int FONT_16=0x114;
        private static final int FONT_18=0x115;

        private static final int PLAIN_ITEM=0x11b;

        private static final int FONT_RED=0x116;
        private static final int FONT_BLUE=0x117;
        private static final int FONT_GREEN=0x118;
        private TextView text;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            text=findViewById(R.id.txt);
    }

        @Override public boolean onCreateOptionsMenu (Menu menu){
            SubMenu fontMenu=menu.addSubMenu("字体大小");
            fontMenu.setIcon(R.drawable.rocket_icon);
            fontMenu.setHeaderTitle("选择字体大小");
            fontMenu.add(0,FONT_10,0,"10号字体");
            fontMenu.add(0,FONT_12,0,"12号字体");
            fontMenu.add(0,FONT_14,0,"14号字体");
            fontMenu.add(0,FONT_16,0,"16号字体");
            fontMenu.add(0,FONT_18,0,"18号字体");

            menu.add(0,PLAIN_ITEM,0,"普通菜单项");

            SubMenu colorMenu=menu.addSubMenu("字体颜色");
            colorMenu.setIcon(R.drawable.cat);
            colorMenu.setHeaderTitle("选择文字颜色");
            colorMenu.add(0,FONT_RED,0,"红色");
            colorMenu.add(0,FONT_GREEN,0,"绿色");
            colorMenu.add(0,FONT_BLUE,0,";蓝色");
            return true;
        }
  @Override public  boolean onOptionsItemSelected(MenuItem mi){
            switch (mi.getItemId()){
                case FONT_10:text.setTextSize(10*2);break;
                case FONT_12:text.setTextSize(12*2);break;
                case FONT_14:text.setTextSize(14*2);break;
                case FONT_16:text.setTextSize(16*2);break;
                case FONT_18:text.setTextSize(18*2);break;
                case FONT_RED:text.setTextColor(Color.RED);break;
                case FONT_GREEN:text.setTextColor(Color.GREEN);break;
                case FONT_BLUE:text.setTextColor(Color.BLUE);break;
                case PLAIN_ITEM:
                Toast.makeText(MainActivity.this, "您点击了普通菜单项", Toast.LENGTH_SHORT).show();break;
            }
return true;
  }
    }