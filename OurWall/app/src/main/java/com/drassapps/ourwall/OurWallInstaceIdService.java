package com.drassapps.ourwall;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class OurWallInstaceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Obtiene el token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {}
}