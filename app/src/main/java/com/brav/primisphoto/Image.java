package com.brav.primisphoto;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brav.primisphoto.customClass.MyRvAdapter;
import com.brav.primisphoto.customInterface.EventCardSelected;
import com.brav.primisphoto.services.FotoInoltrata;
import com.brav.primisphoto.services.InfoFotoInoltrate;
import com.brav.primisphoto.services.Message;
import com.brav.primisphoto.services.PrimisWS;
import com.brav.primisphoto.services.VectorByte;
import com.brav.primisphoto.services.VectorInt32;
import com.brav.primisphoto.util.Alerts;
import com.brav.primisphoto.util.Constants;
import com.brav.primisphoto.util.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
