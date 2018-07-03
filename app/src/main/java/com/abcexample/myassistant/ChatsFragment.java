package com.abcexample.myassistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ViewPager mvpager;

    private OnFragmentInteractionListener mListener;
    private TextToSpeech tts;
    private FloatingActionButton button;

    private RecyclerView mchatlist;
    private DatabaseReference mrootref;
    private DatabaseReference mdatabase;
    private DatabaseReference msgdb;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;
    private Switch automatedswitch;
    private Button Chatwithassistant;



    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        tts=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
                if(status==TextToSpeech.LANG_MISSING_DATA || status==TextToSpeech.LANG_NOT_SUPPORTED)
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Language Missing", Toast.LENGTH_SHORT ).show();
                }
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        if(user!=null){
        mdatabase= FirebaseDatabase.getInstance().getReference().child("messages").child(user.getUid());
        mdatabase.keepSynced(true);
        }
        mrootref= FirebaseDatabase.getInstance().getReference().child("Users");
        mrootref.keepSynced(true);
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        mchatlist=(RecyclerView)view.findViewById(R.id.chatfragmentrecycler);
        mchatlist.setHasFixedSize(true);
        mchatlist.setLayoutManager(new LinearLayoutManager(getActivity()));

        getActivity().setTitle("Chats");
        automatedswitch=(Switch)view.findViewById(R.id.switchforautoresponse);
        button=(FloatingActionButton)view.findViewById(R.id.allusersbutton);
        Chatwithassistant=(Button)view.findViewById(R.id.chatwithassistant);

        mrootref.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String assistant_switch = dataSnapshot.child("switch").getValue().toString();

                if(assistant_switch.equals("true")){
                    automatedswitch.setChecked(true);
                }
                else{
                    automatedswitch.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), AllusersActivity.class);
                startActivity(i);
            }
        });

        Chatwithassistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AssistantActivity.class);
                startActivity(i);
            }
        });

        automatedswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    buttonView.setText("Switch off for Manual Responses   ");
                    mrootref.child(user.getUid()).child("switch").setValue("true");
                }
                else if(isChecked==false){
                    buttonView.setText("Switch On For Automated Response   ");
                    mrootref.child(user.getUid()).child("switch").setValue("false");
                }
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();


        mprogress = new ProgressDialog(getActivity());
        mprogress.setTitle("Loading Data");
        mprogress.setMessage("Please Wait....");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();

        FirebaseRecyclerAdapter<Chats, ChatsFragment.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.UsersViewHolder>(
                Chats.class,
                R.layout.custom,
                ChatsFragment.UsersViewHolder.class,
                mdatabase) {

            @Override
            protected void populateViewHolder(final ChatsFragment.UsersViewHolder viewHolder, final Chats model, int position) {
                final Context context = getActivity().getApplicationContext();
                final String id = getRef(position).getKey();

                mrootref.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("Name").getValue().toString();
                        String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();
                        if(auth.getCurrentUser()!=null) {
                            msgdb=FirebaseDatabase.getInstance().getReference().child("messages").child(user.getUid());
                            Query q = mdatabase.child(id).orderByKey().limitToLast(1);
                            q.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                                        Messages m =child.getValue(Messages.class);
                                        viewHolder.setstatus(m.getMessage(), m.isSeen());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        viewHolder.setDisplayName(name);
                        viewHolder.setthumbnail(thumbnail, context);
                        mprogress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mprogress.dismiss();
                    }
                });

                mprogress.dismiss();
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid", id);
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                });

                viewHolder.mview.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        CharSequence options[]=new CharSequence[]{"Open Profile", "Send Message", "Delete"};

                        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                        builder.setTitle("Select Option");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0){
                                    Intent i=new Intent(getActivity(), ProfileActivity.class);
                                    Bundle bundle=new Bundle();
                                    bundle.putString("userid", id);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                }
                                if(which==1){
                                    Intent i=new Intent(getActivity(), ChatActivity.class);
                                    Bundle bundle=new Bundle();
                                    bundle.putString("userid", id);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                }
                                if(which==2){
                                    AlertDialog.Builder confirmbuilder = new AlertDialog.Builder(getActivity());

                                    confirmbuilder.setTitle("Confirmation");
                                    confirmbuilder.setMessage("Are you sure, You want to Delete Conversation?");

                                    confirmbuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {

                                            mdatabase.child(id).removeValue(new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(getActivity().getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT ).show();
                                                }
                                            });

                                            dialog.dismiss();
                                        }
                                    });

                                    confirmbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                                    AlertDialog alert = confirmbuilder.create();
                                    alert.show();
                                }

                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            }
        };

        mchatlist.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mview;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
        }


        public void setDisplayName(String name)
        {
            TextView Usernameview=(TextView) mview.findViewById(R.id.allusersname);
            Usernameview.setText(name);
        }

        public void setstatus(String status, Boolean seen) {

            TextView userstatus=(TextView)mview.findViewById(R.id.allusersstatus);
            userstatus.setText(status);
        }

        public void setthumbnail(String thumbnail, Context act) {

            CircleImageView thumb_image = (CircleImageView) mview.findViewById(R.id.allusersimage);
            Glide.with(act).load(thumbnail).into(thumb_image);

        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

}
