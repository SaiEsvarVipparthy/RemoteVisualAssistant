package com.example.remotevisualassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserCallActivity extends AppCompatActivity {

    private Button b_end,b_submit,b_call;
    private TextView t,tr,vname,vnum;
    private Button b1,b2,b3,b4,b5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_call);
        set_UI_components();

        b_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                GlobalDefinitions.IsProgramRunning=true;
                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callIntent.setData(Uri.parse("tel:+91"+vnum.getText().toString().trim()));
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(UserCallActivity.this, new String[]{Manifest.permission.CALL_PHONE},10);
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

        b_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String my_id = FirebaseAuth.getInstance().getUid();
                final DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
                codbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        CommunicationOut my_co = dataSnapshot.getValue(CommunicationOut.class);
                        final String to_id = my_co.getId_to();
                        final DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
                        cidbr.child(to_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dSnapshot) {
                                CommunicationIn comin = dSnapshot.getValue(CommunicationIn.class);
                                comin.setCall_cut(true);
                                cidbr.child(to_id).setValue(comin);
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
                b_end.setVisibility(View.INVISIBLE);
                t.setVisibility(View.VISIBLE);
                tr.setVisibility(View.VISIBLE);
                b1.setVisibility(View.VISIBLE);
                b2.setVisibility(View.VISIBLE);
                b3.setVisibility(View.VISIBLE);
                b4.setVisibility(View.VISIBLE);
                b5.setVisibility(View.VISIBLE);
                b_submit.setVisibility(View.VISIBLE);
//                Intent my_intent = new Intent(UserCallActivity.this, UserActivity.class);
//                startActivity(my_intent);
//                finish();
            }
        });

        b_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //log the call
                if(tr.getText().toString().trim().equals("0")){
                    Toast.makeText(UserCallActivity.this,"Please rate to proceed",Toast.LENGTH_SHORT).show();
                }
                else{
                    final String my_id = FirebaseAuth.getInstance().getUid();
                    final DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
                    codbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datanapshot) {
                            CommunicationOut my_co = datanapshot.getValue(CommunicationOut.class);
                            final String to_id = my_co.getId_to();
                            final DatabaseReference logdbr = FirebaseDatabase.getInstance().getReference("call_logs");
                            final CallLog cl = new CallLog(my_id,to_id,my_co.getName_from(),my_co.getName_to(),"0.0",tr.getText().toString().trim());
                            DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
                            cidbr.child(to_id).removeValue();
                            codbr.child(my_id).removeValue();

                            logdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    AllLogs my_logs = dataSnapshot.getValue(AllLogs.class);
                                    List<CallLog> list_all_logs = my_logs.getLogList();
                                    list_all_logs.add(cl);
                                    my_logs.setLogList(list_all_logs);
                                    logdbr.child(my_id).setValue(my_logs);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            logdbr.child(to_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    AllLogs my_logs = dataSnapshot.getValue(AllLogs.class);
                                    List<CallLog> list_all_logs = my_logs.getLogList();
                                    list_all_logs.add(cl);
                                    my_logs.setLogList(list_all_logs);
                                    logdbr.child(to_id).setValue(my_logs);

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

                    Intent my_intent = new Intent(UserCallActivity.this, UserActivity.class);
                    startActivity(my_intent);
                    finish();
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b2.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b3.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b4.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b5.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                tr.setText("1");
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b2.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b3.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b4.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b5.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                tr.setText("2");
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b2.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b3.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b4.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                b5.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                tr.setText("3");
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b2.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b3.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b4.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b5.setBackgroundColor(getResources().getColor(R.color.my_button_grey));
                tr.setText("4");
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b2.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b3.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b4.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                b5.setBackgroundColor(getResources().getColor(R.color.my_yellow_pending));
                tr.setText("5");
            }
        });
    }

    private void set_UI_components(){
        b_end = (Button)findViewById(R.id.button_user_end);
        b_submit = (Button)findViewById(R.id.button_submit);
        b_call = (Button)findViewById(R.id.buttoncall);
        t = (TextView)findViewById(R.id.txtvw);
        tr = (TextView)findViewById(R.id.final_r);
        vname = (TextView)findViewById(R.id.v_name);
        vnum = (TextView)findViewById(R.id.v_number);
        b1 = (Button)findViewById(R.id.b1);
        b2 = (Button)findViewById(R.id.b2);
        b3 = (Button)findViewById(R.id.b3);
        b4 = (Button)findViewById(R.id.b4);
        b5 = (Button)findViewById(R.id.b5);

        tr.setText("0");

        final String my_id = FirebaseAuth.getInstance().getUid();
        DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
        codbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CommunicationOut co = dataSnapshot.getValue(CommunicationOut.class);
                vname.setText(co.getName_to());
                vnum.setText(co.getNumber_to());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
