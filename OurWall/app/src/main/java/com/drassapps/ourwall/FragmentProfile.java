package com.drassapps.ourwall;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentProfile extends Fragment implements AdapterView.OnItemSelectedListener {

    // MARK - CLASS

    private Common ui = new Common();

    // MARK - UI

    private Spinner areaList;

    // MARK - MAIN

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RelativeLayout signOut = getView().findViewById(R.id.profile_logOutLay);

        TextView messagesShare = getView().findViewById(R.id.profile_messageShare);
        TextView imageShare = getView().findViewById(R.id.profile_imageShare);
        TextView soundShare = getView().findViewById(R.id.profile_soundShare);
        TextView userEmail = getView().findViewById(R.id.profile_userEmail);

        Switch noti = getView().findViewById(R.id.profile_activeNoti);
        Switch area = getView().findViewById(R.id.profile_activeZone);

        areaList = getView().findViewById(R.id.profile_areaList);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("userEmail", "");
        userEmail.setText(currentUserEmail);

        setUpSpinner();

        String nMessagesSend = sharedPref.getString("nMessagesSend", "");
        String nImagesSend = sharedPref.getString("nImagesSend", "");
        String nSoundSend = sharedPref.getString("nSoundSend", "");

        messagesShare.setText("Mensajes compartidos: " + nMessagesSend);
        imageShare.setText("Imagenes compartidas: " + nImagesSend);
        soundShare.setText("Música compartia: " + nSoundSend);

        // Noti switch clicked
        noti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    createDialog(getString(R.string.profile_dialogNotiTitle),
                            getString(R.string.profile_dialogNotiMes2),
                            getString(R.string.dialog_button));

                }else {
                    createDialog(getString(R.string.profile_dialogNotiTitle),
                            getString(R.string.profile_dialogNotiMes1),
                            getString(R.string.dialog_button));
                }
            }
        });

        // Area switch clicked
        area.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    createDialog(getString(R.string.profile_dialogNotiTitle),
                            getString(R.string.profile_dialogAreaMes2),
                            getString(R.string.dialog_button));

                }else {
                    createDialog(getString(R.string.profile_dialogNotiTitle),
                            getString(R.string.profile_dialogAreaMes1),
                            getString(R.string.dialog_button));
                }
            }
        });

        // SignOut lay
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        });
    }

    // MARK - METHODS

    private void setUpSpinner() {
        ArrayAdapter<CharSequence> categorieAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.community, R.layout.support_simple_spinner_dropdown_item);
        categorieAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        areaList.setAdapter(categorieAdapter);
    }

    private void createDialog(String title, String message, String dialogButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(dialogButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    // MARK - INTERFACE

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.profile_areaList) {
            switch (parent.getItemAtPosition(pos).toString()){
                case "General":
                    break;
                case "Salón":
                    break;
            }
        }
    }
    public void onNothingSelected(AdapterView<?> parent) { }
}