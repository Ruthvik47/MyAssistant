package com.abcexample.myassistant;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.abcexample.myassistant.AlarmReceiver.REQUEST_CODE;

public class ScheduleActivity extends AppCompatActivity {

    private FloatingActionButton addbutton;
    private RecyclerView schedulerecyclerview;
    private TimePicker Time;
    private Spinner task;
    private EditText customtask;
    private Button saveButton;
    private DatabaseReference scheduleref;
    private DatabaseReference iconref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ProgressDialog mprogress;
    private DatabaseReference schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);


        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        this.setTitle("Daily Schedule");
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        scheduleref= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        schedule= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Schedule");
        iconref=FirebaseDatabase.getInstance().getReference().child("Schedule_Icons");

        schedulerecyclerview=(RecyclerView)findViewById(R.id.schedulerecycler);
        schedulerecyclerview.setHasFixedSize(true);
        schedulerecyclerview.setLayoutManager(new LinearLayoutManager(this));

        addbutton=(FloatingActionButton)findViewById(R.id.activityaddbutton);

        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaydialog();
            }
        });

    }

    private void displaydialog() {
        final Dialog d=new Dialog(this);
        d.setTitle("Add Task");
        d.setContentView(R.layout.customdialog);
        Time=(TimePicker) d.findViewById(R.id.timepicker);
        task=(Spinner) d.findViewById(R.id.taskspinner);
        customtask=(EditText) d.findViewById(R.id.customedittext);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                .getStringArray(R.array.Tasks));//setting the country_array to spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        task.setAdapter(adapter);

        saveButton=(Button) d.findViewById(R.id.save_button);
        d.show();

        task.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(task.getSelectedItem().equals("other")) {
                    customtask.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer hour=Time.getCurrentHour();
                Integer minute=Time.getCurrentMinute();
                String selectedtask= (String) task.getSelectedItem();
                if(selectedtask.equals("other")){
                    selectedtask=customtask.getText().toString();
                }
                Map schedule = new HashMap();
                schedule.put("Hour", hour);
                schedule.put("Minute", minute);
                schedule.put("Task", selectedtask);

                scheduleref.child("Schedule").child(String.valueOf(hour)).updateChildren(schedule, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(getApplicationContext(), "Your Schedule Added Successfully", Toast.LENGTH_SHORT).show();
                        d.dismiss();
                    }
                });

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mprogress = new ProgressDialog(this);
        mprogress.setTitle("Loading Data");
        mprogress.setMessage("Please Wait....");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();

        FirebaseRecyclerAdapter<Schedule, ScheduleActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Schedule, ScheduleActivity.UsersViewHolder>(
                Schedule.class,
                R.layout.custom_schedule,
                ScheduleActivity.UsersViewHolder.class,
                schedule) {
            @Override
            protected void populateViewHolder(final ScheduleActivity.UsersViewHolder viewHolder, Schedule model, int position) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                String time1= hour + ":"+ minute+":"+00;
                String time2=model.getHour() + ":" + model.getMinute()+":"+00;
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date date1 = null;
                try {
                    date1 = format.parse(time1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date2 = null;
                try {
                    date2 = format.parse(time2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long difference = date2.getTime() - date1.getTime();
                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(difference);
                if(minutes<0){
                    minutes+=1440;
                }
                viewHolder.setduration(minutes);
                setalarm(minutes);
                viewHolder.settask(model.getTask());
                viewHolder.settime(model.getHour().toString()+":"+model.getMinute().toString());

                final String id= getRef(position).getKey();
                final String schedulechild=model.getTask().toString();

                iconref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(schedulechild)) {
                            viewHolder.seticon(getApplicationContext(), dataSnapshot.child(schedulechild).getValue().toString());
                        }
                        else{
                            viewHolder.seticon(getApplicationContext(), dataSnapshot.child("other").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.editbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[]=new CharSequence[]{"Edit Task", "Delete"};

                        final AlertDialog.Builder builder=new AlertDialog.Builder(ScheduleActivity.this);
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0){
                                    schedule.child(id).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            displaydialog();
                                        }
                                    });


                                }
                                if(which==1){
                                    schedule.child(id).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            Toast.makeText(getApplicationContext(), "Task Removed Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                        });
                        builder.show();
                    }
                });
            }

        };

        schedulerecyclerview.setAdapter(firebaseRecyclerAdapter);
        mprogress.dismiss();
    }


    @Override
    public void onBackPressed() {
        finish();
        Intent i=new Intent(this, MainActivity.class);
        startActivity(i);
    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mview;
        CircleImageView editbutton;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
            editbutton=(CircleImageView)mview.findViewById(R.id.editbutton);
        }

        public void settime(String time)
        {
            TextView Time=(TextView) mview.findViewById(R.id.scheduletime);
            Time.setText(time);
        }


        public void settask(String task) {
            TextView Task=(TextView) mview.findViewById(R.id.scheduletask);
            Task.setText(task);
        }

        public void setduration(int minutesleft) {
            TextView Duration=(TextView) mview.findViewById(R.id.timeduration);
            Duration.setText(minutesleft +" minutes to go");
        }

        public void seticon(Context c, String image_url) {
            CircleImageView Icon=(CircleImageView) mview.findViewById(R.id.scheduleicon);
            Glide.with(c).load(image_url).into(Icon);
        }

    }

    public void setalarm(int time){
        Intent intent=new Intent(ScheduleActivity.this, AlarmReceiver.class);
        final PendingIntent p1=PendingIntent.getBroadcast(getApplicationContext(),REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager a=(AlarmManager)getSystemService(ALARM_SERVICE);
        a.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + time*60*1000,
                AlarmManager.INTERVAL_DAY ,p1);
    }
}
