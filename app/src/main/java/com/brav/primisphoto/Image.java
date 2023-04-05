package com.brav.primisphoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.GridLayoutManager;
import androidx.appcompat.widget.RecyclerView;

import android.widget.ImageView;

import com.brav.primisphoto.util.Constants;


public class Image extends MyBaseActionBarActivity {

    private Uri imageUri;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setTitle(getString(R.string.title_loadfoto));

        Intent intent = getIntent();
        String uriString = intent.getStringExtra(Constants.EXTRA_IMAGE_URI);
        imageUri = Uri.parse(uriString);

        image = (ImageView) findViewById(R.id.image);
        image.setImageURI(imageUri);
    }

}
