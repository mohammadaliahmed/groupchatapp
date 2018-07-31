package com.appsinventiv.corperstrip.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.appsinventiv.corperstrip.R;
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import com.jaeger.library.StatusBarUtil;

public class ViewPictures extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pictures);
        StatusBarUtil.setTransparent(this);
        Intent i=getIntent();
        String  url=i.getStringExtra("url");
        ImageView img=findViewById(R.id.img);

        Glide.with(this).load(url).placeholder(R.drawable.place).into(img);
        img.setOnTouchListener(new ImageMatrixTouchHandler(this));
    }
}
