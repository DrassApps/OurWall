package com.drassapps.ourwall;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class FragmentShare extends Fragment {

    // MARK - CLASS

    private Common ui = new Common();
    private SharedPreferences.Editor editor;

    // MARK - PROPERTIES

    private static final int PICK_IMAGE = 1;
    private static final int PICK_SOUND = 2;
    private Uri imageUri, soundUri;
    private String type, currentUserEmail, currentRef, lat, lon;
    private String nMessagesSend, nImagesSend, nSoundSend;

    // MARK - UI

    private RelativeLayout photo, text, sound, mLayout, share;
    private TextView infoText, dataText;
    private EditText textToShre;
    private ImageView imagePicked;

    // MARK - MAIN

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        nMessagesSend = sharedPref.getString("nMessagesSend", "");
        nImagesSend = sharedPref.getString("nImagesSend", "");
        nSoundSend = sharedPref.getString("nSoundSend", "");
        currentRef = sharedPref.getString("currentRef", "");
        currentUserEmail = sharedPref.getString("userEmail", "");

        lon = sharedPref.getString("lon", "");
        lat = sharedPref.getString("lat", "");

        photo = getView().findViewById(R.id.share_photoLay);
        text = getView().findViewById(R.id.share_textLay);
        sound  = getView().findViewById(R.id.share_musicLay);
        mLayout = getView().findViewById(R.id.shareFragment);
        share = getView().findViewById(R.id.share_shareLay);

        imagePicked = getView().findViewById(R.id.share_imagePicked);

        infoText = getView().findViewById(R.id.share_infoText);
        dataText = getView().findViewById(R.id.share_dataText);
        textToShre = getView().findViewById(R.id.share_textToShare);

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResourceFromDevice("image",PICK_IMAGE);
                type = "image";
                updateUI(type);
            }
        });
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getResourceFromDevice("audio",PICK_SOUND);
                type = "sound";
                updateUI(type);
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "text";
                updateUI(type);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (type){
                    case "image":
                        uploadFile(imageUri);
                        updateCount("image");
                        break;

                    case "sound":
                        uploadFile(soundUri);
                        updateCount("sound");
                        break;

                    case "text":
                        String userText = textToShre.getText().toString();
                        if (userText.length() > 5) {
                            uploadText(userText);
                            updateCount("text");
                            ui.setSnackBar(mLayout,"Mensaje compartido");
                        }
                        else { ui.setSnackBar(mLayout,getString(R.string.err_lenght)); }
                        break;
                }
            }
        });
    }

    // Update ui for upload a file
    private void updateUI(String type) {
        photo.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        sound.setVisibility(View.GONE);
        share.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.VISIBLE);
        dataText.setVisibility(View.VISIBLE);

        String lastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        dataText.setText("Longitud: "+lon+"\n"+"Latitud: "+lat+"\n"+"Fecha: "+lastUpdateTime);

        switch (type){
            case "image":
                infoText.setText(getString(R.string.shareView_image));
                break;

            case "sound":
                infoText.setText(getString(R.string.shareView_sound));
                break;

            case "text":
                textToShre.setVisibility(View.VISIBLE);
                infoText.setText(getString(R.string.shareView_text));
                break;
        }
    }

    // MARK - METHODS
    private void getResourceFromDevice(String type, int pick) {
        Intent i = new Intent();
        i.setType(type+"/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,getString(R.string.shareView_choose)),pick);
    }

    // Upload file
    private void uploadFile(Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(currentRef);

        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                ui.setSnackBar(mLayout,getString(R.string.shareView_err_upload));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ui.setSnackBar(mLayout,getString(R.string.shareView_suc_upload));
                uploadText("");
            }
        });
    }

    // Upload text and update primary key
    private void uploadText(String text) {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabaseRefUser = mDatabaseRef.child(currentRef);
        ObjectDB objectDB = new ObjectDB(lat,lon,text,currentUserEmail);
        mDatabaseRefUser.setValue(objectDB);
        updateRef();
    }

    private void updateCount(String type) {
        switch (type) {
            case "image":
                editor.putString("nImagesSend", String.valueOf(Integer.valueOf(nImagesSend)+1));
                break;

            case "sound":
                editor.putString("nSoundSend", String.valueOf(Integer.valueOf(nSoundSend)+1));
                break;

            case "text":
                editor.putString("nMessagesSend",String.valueOf(Integer.valueOf(nMessagesSend)+1));
                break;
        }
    }

    // Current ref will be primary key on our db and storage
    private void updateRef() {
        editor.putString("currentRef", String.valueOf(Integer.parseInt(currentRef)+1));
        editor.commit();
    }

    // MARK - RESTUL
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            try {
                if (data != null) {
                    imageUri = data.getData();
                    InputStream imageStream = getActivity().
                            getContentResolver().openInputStream(imageUri);

                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imagePicked.setImageBitmap(selectedImage);
                    imagePicked.setVisibility(View.VISIBLE);

                } else { ui.setSnackBar(mLayout, getString(R.string.err_getData)); }

            } catch (IOException e) { e.printStackTrace(); }
        }

        if (requestCode == PICK_SOUND) {
            if (data != null) {
                soundUri = data.getData();
            }
        }
    }
}
