package com.lehmann.samuel.mazesolver;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    final private int fileIdentifier = R.drawable.hand_maze;
    private boolean setEndPoint = false;
    private boolean startPointSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageHandler imgHandler = new ImageHandler(fileIdentifier, this);
        List<List<WallPixel>> wallPixels = imgHandler.getWalls();
        Bitmap simplifiedBitmap = imgHandler.getSimplifiedBitmap();

        getStartPoint(simplifiedBitmap);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void getStartPoint(Bitmap simplifiedBitmap) {
        setContentView(R.layout.point_selector);
        ImageView img = findViewById(R.id.maze_img_view);


        img.setImageBitmap(simplifiedBitmap);

        img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                ImageView pin;

                if (!setEndPoint) {
                    pin = findViewById(R.id.start_pin);
                    startPointSet = true;
                } else {
                    pin = findViewById(R.id.end_pin);
                }

                pin.setVisibility(View.VISIBLE);

                pin.setX(event.getX() + view.getX() - pin.getWidth() / 2);
                pin.setY(event.getY() + view.getY() - pin.getHeight() / 2);
                return true;
            }
        });

        Button continueButton = findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!setEndPoint) {
                    if (startPointSet) {
                        setEndPoint = true;

                        TextView title = findViewById(R.id.pin_title);
                        title.setText(R.string.endPointPrompt);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.startPointToast, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    //TODO
                }
            }
        });
    }
}
