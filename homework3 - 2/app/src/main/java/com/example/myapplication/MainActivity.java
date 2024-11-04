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


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
    }
    public void simple(View sorce){
        TableLayout login1=(TableLayout) getLayoutInflater().inflate(R.layout.view1,null);
        new AlertDialog.Builder(this).setTitle("ANOROIO APP").setView(login1).setNegativeButton("Cancle",
        (dialog,which)->{
        }).setPositiveButton("Sign in",
                (dialog,which)->{
                }).create().show();
    }
    }