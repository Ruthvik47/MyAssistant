package com.abcexample.myassistant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.telecom.TelecomManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PICK_IMAGE_REQUEST = 71;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Button accountlogoutbutton;
    private TextView accountphonenumber;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private CircleImageView profilepic;
    private String url;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference storageRef;
    private ProgressDialog progress;
    private TextView name;
    private TextView status;
    private Button changestatusbutton;
    private DatabaseReference mdatabase;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        mdatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mdatabase.keepSynced(true);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        storageRef=storage.getReferenceFromUrl("gs://myassistant-8eb5b.appspot.com").child(user.getPhoneNumber()).child("Profilepic");


        View view = inflater.inflate(R.layout.fragment_account, container, false);
        getActivity().setTitle("Account");
        accountlogoutbutton=(Button)view.findViewById(R.id.accountlogoutbutton);
        accountphonenumber=(TextView)view.findViewById(R.id.accountphonenumber);
        profilepic=(CircleImageView)view.findViewById(R.id.profilepic);
        name=(TextView)view.findViewById(R.id.name);
        status=(TextView)view.findViewById(R.id.status);
        changestatusbutton=(Button)view.findViewById(R.id.Changestatusbutton);

        progress=new ProgressDialog(getActivity());

        accountphonenumber.setText(user.getPhoneNumber());
        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("Name").getValue().toString());
                status.setText(dataSnapshot.child("Status").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thumburl=dataSnapshot.child("thumbnail").getValue().toString();
                Glide.with(getActivity())
                        .load(thumburl)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<Bitmap>(100, 100) {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {

                                profilepic.setImageBitmap(bitmap);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(), AccountFragment.this);
            }
        });

        accountlogoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                auth.signOut();
                Intent intent=new Intent(getActivity(), Login_Activity.class);
                startActivity(intent);

            }
        });

        changestatusbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), StatusActivity.class);
                startActivity(i);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK)
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri uri = result.getUri();
            try {
                UploadImagetoServer(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getActivity(), "Crop Activity Error", Toast.LENGTH_LONG).show();
            }

        }
    }


    private void UploadImagetoServer(Uri uri) throws IOException {
        progress.setMessage("Uploading Image...");
        progress.show();
        StorageReference filepath= storageReference.child(user.getPhoneNumber()).child("Profilepic");
        final StorageReference thumbpath= storageReference.child(user.getPhoneNumber()).child("thumbnail");

        File thumb_filepath = new File(uri.getPath());

        Bitmap ImageBitmap = new Compressor(getActivity())
                .setMaxWidth(200)
                .setMaxHeight(200)
                .setQuality(70)
                .compressToBitmap(thumb_filepath);

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        final byte[] image_byte=baos.toByteArray();

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Upload Completed", Toast.LENGTH_LONG).show();
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        url=uri.toString();
                        mdatabase.child("Imageurl").setValue(url);

                        UploadTask uploadtask=thumbpath.putBytes(image_byte);
                        uploadtask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                String thumburl=thumb_task.getResult().getDownloadUrl().toString();
                                if(thumb_task.isSuccessful())
                                {
                                    mdatabase.child("thumbnail").setValue(thumburl);

                                }
                                Glide.with(getActivity())
                                        .load(thumburl)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(profilepic);

                            }
                        });
                    }
                });

                progress.dismiss();
            }
        });

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
}
