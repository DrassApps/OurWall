package com.drassapps.ourwall;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class FragmentShare extends Fragment {

    private RelativeLayout photo, text, sound, mLayout, share;
    public static final int PICK_IMAGE = 1;
    public static final int PICK_SOUND = 2;

    private Bitmap selectedImage;
    private Uri imageUri, soundUri;

    private TextView infoText, dataText;
    private EditText textToShre;
    private Common ui = new Common();

    private String type;
    private ImageView imagePicked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                // TODO: USAR RETROFIT PARA SUBIR LOS DATOS AL SERVIDOR Y ACTUALIZAR EL MAPA
                switch (type){
                    case "image":
                        break;

                    case "sound":
                        break;

                    case "text":
                        textToShre.getText().toString();
                        if (textToShre.length() > 5) {

                        }else {
                            ui.setSnackBar(mLayout,"Introduce un texto más largo");
                        }
                        break;
                }
            }
        });
    }

    private void updateUI(String type) {
        photo.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        sound.setVisibility(View.GONE);
        share.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.VISIBLE);
        dataText.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String lon = sharedPref.getString("lon", "");
        String lat = sharedPref.getString("lat", "");

        String lastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        dataText.setText("Longitud: "+lon+"\n"+"Latitud: "+lat+"\n"+"Fecha: "+lastUpdateTime);

        switch (type){
            case "image":
                infoText.setText("Vas a añadir una imagen");
                break;

            case "sound":
                infoText.setText("Vas a añadir un sonudi");
                break;

            case "text":
                textToShre.setVisibility(View.VISIBLE);
                infoText.setText("Vas a añadir un texto");
                break;
        }
    }

    // MARK - GET DATA FROM DEVICE
    private void getResourceFromDevice(String type, int pick) {
        Intent intent = new Intent();
        intent.setType(type+"/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Elige una"), pick);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Si ha elegido una imagen
        if (requestCode == PICK_IMAGE) {
            try {
                // Si tenemos datos
                if (data != null) {

                    // Obtenemos una URI de la ubicación de la imagen
                    imageUri = data.getData();

                    // Abrimos la URI a través de un InputStram
                    final InputStream imageStream = getActivity()
                            .getContentResolver().openInputStream(imageUri);

                    // Convertimos ese inputstream a un BitMap
                    selectedImage = BitmapFactory.decodeStream(imageStream);

                    // La asignamos a la ImagevIEW
                    imagePicked.setImageBitmap(selectedImage);
                    imagePicked.setVisibility(View.VISIBLE);

                    // Si no hay data significa que el usuario no ha elegido ninguna imagen
                } else {
                    ui.setSnackBar(mLayout, "meh");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Si ha elegido una cacion
        if (requestCode == PICK_SOUND) {
            // Si tenemos datos
            if (data != null) {

                // Obtenemos una URI de la ubicación del sonido
                soundUri = data.getData();

            }
        }
    }
}
