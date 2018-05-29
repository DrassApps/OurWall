package com.drassapps.ourwall;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentFooter extends Fragment {

    // MARK - CLASS

    private FragmentMap  classMap  = new FragmentMap();
    private FragmentShare classShare = new FragmentShare();
    private FragmentProfile classUser = new FragmentProfile();

    // MARK - PROPERTIES
    private boolean firstTime = true;
    private String lastUsedFragment = "";

    private static final String sUserMap = "classMap";
    private static final String sUserShare = "userShsare";
    private static final String sUserProfile = "clasProfile";

    // MARK - UI
    private RelativeLayout profile, map, share;
    private TextView profileText, mapText, shareText;
    private Button profileButton, mapButton, shareButton;
    private Button profileButtonLarge, mapButtonLarge, shareButtonLarge;

    // MARK - LIFE CYCLE

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        return inflater.inflate(R.layout.footer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        map = getView().findViewById(R.id.footerMenu_map);
        share = getView().findViewById(R.id.footerMenu_share);
        profile = getView().findViewById(R.id.footerMenu_profile);

        mapText = getView().findViewById(R.id.footerTV_map);
        shareText = getView().findViewById(R.id.footerTV_share);
        profileText  = getView().findViewById(R.id.footerTV_profile);

        mapButton = getView().findViewById(R.id.footerBT_map);
        shareButton = getView().findViewById(R.id.footerBT_share);
        profileButton  = getView().findViewById(R.id.footerBT_profile);

        mapButtonLarge = getView().findViewById(R.id.footerBTLarge_map);
        shareButtonLarge = getView().findViewById(R.id.footerBTLarge_share);
        profileButtonLarge = getView().findViewById(R.id.footerBTLarge_profile);

        profileButtonLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setPressed(true);
                replaceFragment(classUser, sUserProfile);
            }
        });
        mapButtonLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setPressed(true);
                replaceFragment(classMap, sUserMap);
            }
        });
        shareButtonLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share.setPressed(true);
                replaceFragment(classShare, sUserShare);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { replaceFragment(classUser, sUserProfile); }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { replaceFragment(classMap, sUserMap);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { replaceFragment(classShare, sUserShare); }
        });
    }

    // MARK - ACTIONS
    private void updateFragment(Fragment newFragment, String usedFragment, boolean detach) {
        if (detach){
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.activityuser_userFragment, newFragment)
                    .detach(classMap)
                    .commit();
        }else {
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.activityuser_userFragment, newFragment)
                    .commit();
        }
        lastUsedFragment = usedFragment;
    }
    private void updateUi(String currentFragment) {
        switch (currentFragment) {
            case sUserProfile:
                profileText.setVisibility(View.VISIBLE);
                profileButton.setVisibility(View.VISIBLE);
                profileButtonLarge.setVisibility(View.INVISIBLE);

                mapButtonLarge.setVisibility(View.VISIBLE);
                shareButtonLarge.setVisibility(View.VISIBLE);

                mapButton.setVisibility(View.INVISIBLE);
                mapText.setVisibility(View.INVISIBLE);
                shareButton.setVisibility(View.INVISIBLE);
                shareText.setVisibility(View.INVISIBLE);
                profile.setPressed(false);
                break;

            case sUserMap:

                mapText.setVisibility(View.VISIBLE);
                mapButton.setVisibility(View.VISIBLE);
                mapButtonLarge.setVisibility(View.INVISIBLE);

                profileButtonLarge.setVisibility(View.VISIBLE);
                shareButtonLarge.setVisibility(View.VISIBLE);

                profileButton.setVisibility(View.INVISIBLE);
                profileText.setVisibility(View.INVISIBLE);
                shareButton.setVisibility(View.INVISIBLE);
                shareText.setVisibility(View.INVISIBLE);
                map.setPressed(false);
                break;

            case sUserShare:

                shareText.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);
                shareButtonLarge.setVisibility(View.INVISIBLE);

                profileButtonLarge.setVisibility(View.VISIBLE);
                mapButtonLarge.setVisibility(View.VISIBLE);

                profileButton.setVisibility(View.INVISIBLE);
                profileText.setVisibility(View.INVISIBLE);
                mapButton.setVisibility(View.INVISIBLE);
                mapText.setVisibility(View.INVISIBLE);
                share.setPressed(false);
                break;
        }
    }

    // MARK - APP WORKFLOW
    public void replaceFragment(Fragment newFragment, String fragmentClicked) {
        switch (fragmentClicked) {
            case sUserMap:
                switch (lastUsedFragment) {
                    case sUserProfile:
                        updateUi(sUserMap);
                        getActivity().getFragmentManager().beginTransaction()
                                .replace(R.id.activityuser_userFragment, newFragment)
                                .attach(classUser)
                                .commit();
                        lastUsedFragment = sUserMap;
                        break;
                    case sUserShare:
                        updateUi(sUserMap);
                        getActivity().getFragmentManager().beginTransaction()
                                .replace(R.id.activityuser_userFragment, newFragment)
                                .attach(classShare)
                                .commit();
                        lastUsedFragment = sUserMap;
                        break;
                }
                break;
            case sUserProfile:
                if (firstTime) {
                    updateUi(sUserProfile);
                    updateFragment(newFragment,sUserProfile,true);
                    firstTime = false;
                    break;
                } else {
                    updateUi(sUserProfile);
                    updateFragment(newFragment,sUserProfile,false);
                    break;
                }
            case sUserShare:
                if (firstTime) {
                    updateUi(sUserShare);
                    updateFragment(newFragment,sUserShare,true);
                    firstTime = false;
                    break;
                } else {
                    updateUi(sUserShare);
                    updateFragment(newFragment,sUserShare,false);
                    break;
                }
        }
    }
}
