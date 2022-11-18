package com.example.h4opslagstavlen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private Button mButtonClear;
    private RelativeLayout mViewLayout;
    private RelativeLayout mainLayout;
    private Button mButtonSave;
    private ImageView latestImage;
    private TextView apiReponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button_id);
        mainLayout = findViewById(R.id.main_layout);
        mButtonClear = findViewById(R.id.button_clear);
        mViewLayout = findViewById(R.id.image_layout);
        mButtonSave = findViewById(R.id.button_save);
        apiReponse = (TextView) findViewById(R.id.save_images_response);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GetImageFromAPI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearImageView();
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveImageToApi();
            }
        });

    }

    /***
     * Returns a new OnTouchListener
     *
     * @return
     */
    private View.OnTouchListener ImageDraggable() {
        return new View.OnTouchListener() {
            float x, y;
            float dx, dy;
            int xDelta, yDelta;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int x = (int) motionEvent.getRawX();
                final int y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    //If mouse is held down
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                view.getLayoutParams();

                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                                .getLayoutParams();
                        layoutParams.leftMargin = x - xDelta;
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;
                }
                mainLayout.invalidate();
                return true;
            }
        };
    }

    /***
     * Gets a base 64 string from api
     * Converts BASE64 string to bitmap and creates a new Imageview
     * And sets the bitmap to the Imageview, then adds to the Image Layout which is a relativelayout
     * Last add a ontouch event to the new imageview
     *
     * @throws IOException
     */
    private void GetImageFromAPI() throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.108.137.211:5003/api/Image/GetImage";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful())
                {
                    String responsetext = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] decodeString = Base64.decode(responsetext, Base64.DEFAULT);
                            Bitmap picture = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length);
                            ImageView view = new ImageView(MainActivity.this);
                            view.setImageBitmap(picture);
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(600, 1000);
                            params.setMargins(2, 2, 2, 2);
                            view.setLayoutParams(params);
                            mViewLayout.addView(view);
                            view.setOnTouchListener(ImageDraggable());
                            latestImage = view;
                        }
                    });
                }
            }
        });
    }

    /***
     * Removes all views that are inside our Image Layout(RelativeLayout)
     */
    private void ClearImageView()
    {
        mViewLayout.removeAllViews();
    }

    private void SaveImageToApi()
    {
        ImageView view = latestImage;
        BitmapDrawable drawable = (BitmapDrawable) view.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.108.137.211:5003/api/Image/SaveImage";
        RequestBody body = new FormBody.Builder().add("base64", encoded).build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    apiReponse.setText("Image Saved");
                }
            }
        });
    }

}