package com.lehmann.samuel.mazesolver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    final private int fileIdentifier = R.drawable.hand_maze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageHandler imgHandler = new ImageHandler(fileIdentifier, this);
    }
}
