package com.brav.primisphoto;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.GridLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.brav.primisphoto.util.GPSManager2;
import com.brav.primisphoto.util.PrimisType;
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


public class Photo extends MyBaseActivity implements DialogInterface.OnClickListener, EventCardSelected {


    public final static int CAMERA_INTENT_CALLED = 5;
    public final static int CODE_CAMERA_PERMISSION = 10;
    public final static int GALLERY_INTENT_CALLED = 6;
    public final static int GALLERY_KITKAT_INTENT_CALLED = 7;
    public final static int CODE_IMAGE_BIG = 50;
    private static final int MAX_N_FOTO = 20;

    private int maxLengthNote;

    private Intent tmp_intent = null;
    private Uri lastUriCamera = null;

    private ProgressDialog dialog;
    private Runnable changeText = new Runnable() {
        @Override
        public void run() {
            if (dialog != null && dialog.isShowing())
                dialog.setMessage(getString(R.string.progress_send));
        }
    };

    private Runnable updateProgressDialog = new Runnable() {
        @Override
        public void run() {
            if (dialog != null && dialog.isShowing())
                dialog.setProgress(tmp_int);
        }
    };

    private Runnable hideDialog = new Runnable() {
        @Override
        public void run() {
            if (dialog != null && dialog.isShowing())
                dialog.hide();
            dialog = null;
        }
    };

    private EditText edt_note;
    private MyRvAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Uri> myDataset;
    private ArrayList<Uri> externalUri;
    private String n_scheda;
    private String password;
    private RecyclerView rv_gallery;
    private Runnable showDialog = new Runnable() {
        @Override
        public void run() {
            dialog = ProgressDialog.show(getApplicationContext(), "", getString(R.string.progress), true);
        }
    };
    private String username;
    private PrimisWS ws;
    private TextView num_foto;

    private int tmp_int = 0;
    private int tmp_max = 0;

    private boolean needGPS = false;
    private Location position = null;

    private Message SaveFotoInoltrate() {

        List<Integer> indexes = new ArrayList<>();
        List<Boolean> res = new ArrayList<>();
        tmp_int = 0;
        for (int i = 0; i < myDataset.size(); i++) {

            Uri uri = myDataset.get(i);
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            byte[] byteArray = stream.toByteArray();
            Message message = UploadPic(ws, byteArray, i, "jpeg");
            if(!TextUtils.isEmpty(message.note))
                indexes.add(Integer.parseInt(message.note));
            res.add(message.ok);
            tmp_int = i;
            runOnUiThread(updateProgressDialog);

        }

        runOnUiThread(changeText);

        InfoFotoInoltrate ifi = new InfoFotoInoltrate();
        ifi.note = edt_note.getText().toString();
        ifi.numFoto = myDataset.size();
        ifi.numScheda = n_scheda;
        ifi.username = username;
        ifi.password = password;
        ifi.schedeAllegatiIds = new VectorInt32(indexes);
        Message mm = ws.SaveFotoInoltrateServer(ifi);
        return mm;
    }

    private DialogInterface.OnClickListener enableGPSSettings = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent gpsOptionsIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(gpsOptionsIntent);
        }
    };

    private void addImageFromCamera(Intent data) {
        addImageFromGallery(data);
    }

    private boolean addImageFromGallery(Intent data) {
        if (data == null)
            return false;
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            Log.v("immagini", "immagini singola  " + selectedImageUri.toString());
            addUri(selectedImageUri);
        }else{
            addUri(lastUriCamera);
            lastUriCamera = null;
        }
        return true;
    }

    private void addImagesFromGallery(Intent data) {
        if (data == null)
            return;

        boolean multiple = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (data.getClipData() != null) {
                multiple = true;
                // retrieve a collection of selected images
                ClipData mClipData = data.getClipData();
                // iterate over these images
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();

                    Log.v("immagini", "immagini  " + uri.toString());
                    addUri(uri);
                }
            }
        }

        if(!multiple)
            addImageFromGallery(data);
    }



    private void addUri(Uri uri) {
        boolean canAdd = true;

        // su molti samsung abbiamo path diversi a seconda dell'applicazione che li legge
        if (uri.toString().startsWith("content://com.google.android.apps.photos.content")){
            int startIndex = uri.toString().indexOf("/content");
            int endIndex = uri.toString().indexOf("/ORIGINAL");
            if(endIndex == -1)
                endIndex = uri.toString().indexOf("/REQUIRE_ORIGINAL");
            if(endIndex == -1)
                endIndex = uri.toString().indexOf("/LARGE");
            if(endIndex!=-1) {
                String str = uri.toString().substring(startIndex + 1, endIndex);
                uri = Uri.parse(Uri.decode(str));
            }
        }

        for (Uri _uri : externalUri) {
            if (_uri == uri || _uri.getPath().equalsIgnoreCase(uri.getPath())) {
                canAdd = false;
            }
        }

        if (canAdd) {
            if (myDataset.size() < MAX_N_FOTO) {

                saveToInternalStorage(uri);
                mAdapter.notifyItemInserted(myDataset.size() - 1);
            } else
                Alerts.promptOK("", R.string.attenzione_max_photo, this, null);
        }

        setNum_foto(myDataset.size());
    }

    private void saveToInternalStorage(Uri _externalUri){

        Bitmap bitmapImage = null;
        Bitmap resized = null;
        try {
            bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), _externalUri);
            if(bitmapImage.getWidth() > 1000 && bitmapImage.getHeight() > 1000) {
                int newWidth,newHeight;
                if(bitmapImage.getWidth() > bitmapImage.getHeight())
                {
                    newHeight = 720;
                    newWidth = Math.round(bitmapImage.getWidth()*newHeight/(float)bitmapImage.getHeight());
                }else {
                    newWidth = 720;
                    newHeight = Math.round(bitmapImage.getHeight()*newWidth/(float)bitmapImage.getWidth());
                }
                resized = Bitmap.createScaledBitmap(bitmapImage, newWidth, newHeight, true);
                bitmapImage = resized;
            }
        }catch (Exception io){
            Log.v("exception", "ex " + io.getMessage());
        }



        //save internal and resize!!!
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(Constants.INTERNAL_IMAGE_FOlDER, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,imageFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            if(bitmapImage!=null)
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(fos!=null){
            externalUri.add(_externalUri);
            myDataset.add((Uri.fromFile(mypath)));
        }

        if(bitmapImage != null)
            bitmapImage.recycle();

        if(resized != null)
            resized.recycle();
    }

    private void openCamera() {

        if(Build.VERSION.SDK_INT >= 23) {
            int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission2 != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_CAMERA_PERMISSION);
                return;
            }
        }
        Uri mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new ContentValues());
        lastUriCamera = mPhotoUri; //Uri.parse(root+timeStamp+"_photo.jpeg");
        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            if (permission == PackageManager.PERMISSION_GRANTED ) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(intent, CAMERA_INTENT_CALLED);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_CAMERA_PERMISSION);
            }
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, lastUriCamera);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_INTENT_CALLED);
            }
        }
    }


    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { //Build.VERSION_CODES.KITKAT
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_KITKAT_INTENT_CALLED);
            } catch (Exception e) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_INTENT_CALLED);
            }
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, GALLERY_INTENT_CALLED);
        }

    }

    private void responseSaveFotoInoltrate(Object... value) {

        //if(value == null || (value.length == 1 && value[0] == null)){
        //    Alerts.promptOK("",R.string.errore, this, null);
        //}else
        if (value != null && value.length == 1 && value[0] != null) {
            Message response = (Message) value[0];
            if (response.note == null)
                return;
            if (response.ok)
                Alerts.promptOK("", response.note, this, this);
            else
                Alerts.promptOK("", response.note, this, null);
        }


    }

    private void setNum_foto(int num){
        num_foto.setText(getResources().getString(R.string.num) + " " + num+"/"+MAX_N_FOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GALLERY_INTENT_CALLED:
                    addImageFromGallery(data);
                    break;
                case GALLERY_KITKAT_INTENT_CALLED:

                    dialog = ProgressDialog.show(this, "", getString(R.string.progress), true);
                   // addImagesFromGallery(data);
                    tmp_intent = data;
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addImagesFromGallery(tmp_intent);
                            runOnUiThread(hideDialog);
                        }
                    }, 1000);

                    break;
                case CAMERA_INTENT_CALLED:
                    addImageFromCamera(data);
            }
        }

        if(resultCode == RESULT_OK && requestCode == CAMERA_INTENT_CALLED && lastUriCamera != null && data == null){
            addUri(lastUriCamera);
            lastUriCamera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CODE_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case GPSManager2.CODE_GPS_PERMISSION:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //openCamera();

                } else {
                    Alerts.promptOK("",R.string.errore_gps_permission, this, null);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        //rimuovo le immagini interne che ho spedito!!!
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(Constants.INTERNAL_IMAGE_FOlDER, Context.MODE_PRIVATE);
        Utility.deleteRecursive(directory);

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getSupportActionBar().setCustomView(R.layout.toolbar_top_photo);
        setTitle(getString(R.string.title_loadfoto));

        Intent intent = getIntent();
        username = intent.getStringExtra(Constants.USERNAME);
        password = intent.getStringExtra(Constants.PASSWORD);
        n_scheda = intent.getStringExtra(Constants.N_SCHEDA);
        maxLengthNote = intent.getIntExtra(Constants.N_MAXLENNOTE, 300);

        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Constants.URIs_photo);
        tmp_intent = null;
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
            tmp_intent = intent;
        }

        ws = new PrimisWS(this);
        if(Constants.primisType == PrimisType.Red) {
            ws.setUrl(Constants.Url_primis_red);
        }else{
            ws.setUrl(Constants.Url_primis_green);
        }


        rv_gallery = (RecyclerView) findViewById(R.id.rv_gallery);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rv_gallery.setHasFixedSize(true);


        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this, 3);
        rv_gallery.setLayoutManager(mLayoutManager);

        myDataset = new ArrayList<>();
        externalUri = new ArrayList<>();

        ((TextView) findViewById(R.id.n_scheda)).setText(n_scheda.toUpperCase());
        num_foto = findViewById(R.id.num_foto);
        setNum_foto(0);



        edt_note = findViewById(R.id.note);
        edt_note.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLengthNote)});

        // specify an adapter
        mAdapter = new MyRvAdapter(myDataset, this);
        rv_gallery.setAdapter(mAdapter);

        setNum_foto(myDataset.size());


        Handler h = new Handler();
        if(tmp_intent!= null) {
            dialog = ProgressDialog.show(this, "", getString(R.string.progress), true);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Uri> imageUris = tmp_intent.getParcelableArrayListExtra(Constants.URIs_photo);
                    for (int i = 0; i < imageUris.size(); i++) {
                        Log.v("immagini", "immagini  " + imageUris.get(i).toString());
                        addUri(imageUris.get(i));
                    }
                    runOnUiThread(hideDialog);
                    tmp_intent = null;
                }
            }, 1000);
        }


        GPSManager2.getInstance(this);

    }

    @Override
    public void selectedItem() {
        findViewById(R.id.removePhoto).setVisibility(View.VISIBLE);
    }

    @Override
    public void deselectedItem() {
        //controllo se non ci sono altri elementi selezionati e se è così rimuovo il cestino dalla barra in alto
        boolean hideBtn = true;
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            View v = rv_gallery.getLayoutManager().findViewByPosition(i);
            CheckBox ck = v.findViewById(R.id.selected);
            if(ck.isChecked()){
                hideBtn = false;
            }
        }

        if (hideBtn)
            findViewById(R.id.removePhoto).setVisibility(View.GONE);
    }

    private void removeElementSelected(){
        for (int i = mAdapter.getItemCount()-1; i >= 0 ; i--) {
            View v = rv_gallery.getLayoutManager().findViewByPosition(i);
            CheckBox ck = v.findViewById(R.id.selected);
            if(ck.isChecked()){
                v.setSelected(false);
                myDataset.remove(i);
                externalUri.remove(i);
                mAdapter.notifyItemRemoved(i);
                mAdapter.notifyItemRangeChanged(i, myDataset.size());
            }
        }
        setNum_foto(myDataset.size());
        findViewById(R.id.removePhoto).setVisibility(View.GONE);
    }

    @Override
    public void showItem(Uri uri) {
        Intent intent = new Intent(this, Image.class);
        intent.putExtra(Constants.EXTRA_IMAGE_URI, uri.toString());
        startActivityForResult(intent, CODE_IMAGE_BIG);
    }

    public Message UploadPic(PrimisWS MyWebService, byte[] buffer, int index, String type) {
        Message mm = new Message();
        mm.ok = false;
        mm.note = "";

        String note = edt_note.getText().toString();
        if(TextUtils.isEmpty(note))
            note = "";
        if (buffer != null) {
            Message msg;
            FotoInoltrata fi = new FotoInoltrata();
            fi.foto = new VectorByte(buffer);
            fi.indiceFoto = index;
            fi.estensioneFoto = type;
            fi.numScheda = n_scheda;
            fi.username = username;
            fi.password = password;
            if(needGPS) {
                fi.latitudine = String.valueOf(position.getLatitude()); //(Float) position.getLatitude()
                fi.longitudine = String.valueOf(position.getLongitude());
            }else {
                fi.latitudine = "";
                fi.longitudine = "";
            }
            fi.note = note;
            msg = MyWebService.SaveFotoInoltratePics(fi);
            if (msg != null)
                return msg;
            else
                return mm;
        }
        return mm;
    }



    public void onClickSendGPS(View target) {

        GPSManager2.resetAsk();
        if(!GPSManager2.checkPermission(this)){
            GPSManager2.resetAsk();
            GPSManager2.getInstance(this);
            return;
        }


        if(!GPSManager2.isLocationEnabled(target.getContext())){
            Alerts.promptYesNo("",R.string.errore_gps_disable, R.string.enable, R.string.close, this, enableGPSSettings, null);
            return;
        }

        position = GPSManager2.getInstance(this).getPosition();
        if(position == null){
            Alerts.promptOK("",R.string.errore_gps_non_trovato, this, null);
            return;
        }
        //Volendo inserire un ulteriore step che dice quanto è precisa la posizione ottenuta
        // *else if( position.getAccuracy() < 40){
        // return; }

        needGPS = true;
        position = GPSManager2.getInstance(this).getPosition();
        sendPhoto();
    }

    public void onClickSend(View target) {
        needGPS = false;
        sendPhoto();
    }

    private void sendPhoto(){
        if(myDataset.size() == 0){
            Alerts.promptOK("",R.string.err_photo, this, null);
            return;
        }

        setShowProgressDialog(0, myDataset.size());
        new wsTask().execute();
    }

    public void setShowProgressDialog(int status, int max){
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.progress_send));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();

        dialog.setProgress(status);
        dialog.setMax(max);
    }

    public void onClickTopBar(View target) {
        switch (target.getId()) {
            case R.id.removePhoto:
                removeElementSelected();
                break;
            case R.id.makePhoto:
                openCamera();
                break;
            case R.id.openGallery:
                openGallery();
                break;
        }
    }

    private class wsTask extends AsyncTask<Object, Object, Object> {


        public wsTask() {

        }

        @Override
        protected Object doInBackground(Object... params) {
            this.publishProgress(SaveFotoInoltrate());
            return null;
        }


        @Override
        public void onProgressUpdate(Object... value) {
            if (dialog != null)
                dialog.dismiss();
            responseSaveFotoInoltrate(value);
        }
    }



}
