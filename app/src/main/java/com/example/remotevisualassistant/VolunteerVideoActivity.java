package com.example.remotevisualassistant;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VolunteerVideoActivity extends FragmentActivity implements OnMapReadyCallback{
//        extends FragmentActivity implements OnMapReadyCallback {

    private TextView in_name, in_number,in_url;
    private WebView webView;
    private Button b_play, b_call, b_gps;
    private GoogleMap mMap;
    private double in_lat, in_long;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_video);

        set_UI_components();

        final String my_id = FirebaseAuth.getInstance().getUid();
        final DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
        cidbr.child(my_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                boolean cut = dataSnapshot.getValue(boolean.class);
                if(cut){
                    android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(VolunteerVideoActivity.this);
                    builder.setTitle("Call Ended");
                    builder.setMessage("User ended the call. press okay to return to volunteer home");
                    builder.setCancelable(false);
                    builder.setPositiveButton(
                            "okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    Intent my_intent = new Intent(VolunteerVideoActivity.this,VolunteerActivity.class);
                                    startActivity(my_intent);
                                    finish();
                                }
                            }
                    );
                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        final String my_id = FirebaseAuth.getInstance().getUid();
        DatabaseReference cindbr = FirebaseDatabase.getInstance().getReference("in_comms");
        cindbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CommunicationIn c_in = dataSnapshot.getValue(CommunicationIn.class);
                final String from_id = c_in.getId_from();
                DatabaseReference locdbr = FirebaseDatabase.getInstance().getReference("last_location");
                locdbr.child(from_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserLocation ut = dataSnapshot.getValue(UserLocation.class);
                        in_lat = ut.getLat();
                        in_long = ut.getLon();
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map2);
                        mapFragment.getMapAsync(VolunteerVideoActivity.this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        b_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.VISIBLE);
                webView.bringToFront();

                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setDisplayZoomControls(false);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.loadUrl(in_url.getText().toString());
            }
        });



        b_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.INVISIBLE);

            }
        });

        b_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:+91"+in_number.getText().toString().trim()));
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(VolunteerVideoActivity.this, new String[]{Manifest.permission.CALL_PHONE},10);
                    return;
                }
                else{
                    try{
                        startActivity(callIntent);
                    }
                    catch(android.content.ActivityNotFoundException e){
                        Toast.makeText(getApplicationContext(),"Activity not found",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng loc = new LatLng(in_lat, in_long);
        mMap.addMarker(new MarkerOptions().position(loc).title("Marker in Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,16.0f));
    }

    private void set_UI_components(){
        in_name = (TextView)findViewById(R.id.text_caller_name);
        in_number = (TextView)findViewById(R.id.text_caller_number);
        in_url = (TextView)findViewById(R.id.text_vidurl);
        //videoView = (VideoView)findViewById(R.id.videoView);
//        mapView = (MapView)findViewById(R.id.mapView);
        webView = (WebView)findViewById(R.id.webView);
        b_play = (Button) findViewById(R.id.imageButton_play);
        b_call = (Button) findViewById(R.id.imageButton_call);
        b_gps = (Button) findViewById(R.id.imageButton_gps);
//        b_end =(ImageButton)findViewById(R.id.imageButton_end);

        String id = FirebaseAuth.getInstance().getUid();
        DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
        cidbr.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CommunicationIn my_ci = dataSnapshot.getValue(CommunicationIn.class);
                in_name.setText(my_ci.getName_from());
                in_number.setText(my_ci.getNumber_from());
                in_url.setText(my_ci.getVid_url());
                String from_id = my_ci.getId_from();
//                DatabaseReference locdbr = FirebaseDatabase.getInstance().getReference("last_location");
//                locdbr.child(from_id).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dSnapshot) {
//                        UserTracking ut = dSnapshot.getValue(UserTracking.class);
//                        LatLng usrcoord = new LatLng(Double.parseDouble(ut.getLat()),Double.parseDouble(ut.getLng()));
//                        Location usrloc = new Location("");
//                        usrloc.setLatitude(Double.parseDouble(ut.getLat()));
//                        usrloc.setLongitude(Double.parseDouble(ut.getLng()));
//                        mGoogleMap.addMarker(new MarkerOptions()
//                                                    .position(usrcoord)
//                                                    .title("User").snippet("snippet")
//                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usrcoord,12.0f));
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
