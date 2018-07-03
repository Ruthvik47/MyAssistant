package com.abcexample.myassistant;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.api.services.vision.v1.model.Color;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Created by divya on 10-02-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Messages> mMessageslist;
    private FirebaseAuth mauth;
    private FirebaseUser user;
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private Context context;

    public MessageAdapter(List<Messages> mMessageslist) {
        this.mMessageslist = mMessageslist;
    }
    public void add(Messages messages) {
        mMessageslist.add(messages);
        notifyItemInserted(mMessageslist.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder=null;

        context= parent.getContext();

        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.message_layout, parent, false);
                viewHolder=new MyChatViewHolder(viewChatMine);
                break;

            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.outgoingmessage_layout, parent, false);
                viewHolder=new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser();
        Messages c = mMessageslist.get(position);
        String from_user=c.getFrom();
        if (from_user.equals(user.getUid().toString())) {
            configureMyChatViewHolder((MyChatViewHolder) holder, position);
        }
        else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
        }

    }

    private void configureMyChatViewHolder(MyChatViewHolder myChatViewHolder, int position) {
        Messages c = mMessageslist.get(position);

        if(c.getType().equals("text")) {
            myChatViewHolder.messageimage2.setVisibility(View.GONE);
            myChatViewHolder.timetext2.setVisibility(View.VISIBLE);
            myChatViewHolder.messagetext2.setVisibility(View.VISIBLE);

            myChatViewHolder.messagetext2.setText(c.getMessage());
            GetTimeAgo gettime = new GetTimeAgo();
            long lasttime = Long.parseLong(String.valueOf(c.getTime()));
            String lastseentime = gettime.getTimeAgo(lasttime, myChatViewHolder.timetext2.getContext());
            myChatViewHolder.timetext2.setText(lastseentime);
        }
        else if(c.getType().equals("image")){
            myChatViewHolder.messagetext2.setVisibility(View.GONE);
            myChatViewHolder.timetext2.setVisibility(View.GONE);
            myChatViewHolder.messageimage2.setVisibility(View.VISIBLE);

            Glide.with(myChatViewHolder.messageimage2.getContext()).load(c.getMessage())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.logo).into(myChatViewHolder.messageimage2);

        }

    }

    private void configureOtherChatViewHolder(OtherChatViewHolder otherChatViewHolder, int position) {
        Messages c = mMessageslist.get(position);

        if(c.getType().equals("text")) {
            otherChatViewHolder.messageimage.setVisibility(View.GONE);
            otherChatViewHolder.messagetext.setVisibility(View.VISIBLE);
            otherChatViewHolder.timetext.setVisibility(View.VISIBLE);

            otherChatViewHolder.messagetext.setText(c.getMessage());
            GetTimeAgo gettime = new GetTimeAgo();
            long lasttime = Long.parseLong(String.valueOf(c.getTime()));
            String lastseentime = gettime.getTimeAgo(lasttime, otherChatViewHolder.timetext.getContext());
            otherChatViewHolder.timetext.setText(lastseentime);
        }
        else if(c.getType().equals("image")){
            otherChatViewHolder.messagetext.setVisibility(View.GONE);
            otherChatViewHolder.timetext.setVisibility(View.GONE);
            otherChatViewHolder.messageimage.setVisibility(View.VISIBLE);

            Glide.with(otherChatViewHolder.messageimage.getContext()).load(c.getMessage())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.logo).into(otherChatViewHolder.messageimage);
        }
    }


    public class OtherChatViewHolder extends RecyclerView.ViewHolder{
        private TextView messagetext;
        private TextView timetext;
        private ImageView messageimage;
        private LinearLayout mlinear;

        public OtherChatViewHolder (View view){
            super(view);
            messagetext=(TextView)view.findViewById(R.id.outgoingmessage);
            timetext=(TextView)view.findViewById(R.id.outgoingmessagetimestamp);
            messageimage=(ImageView)view.findViewById(R.id.outgoingmessage_image);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messagetext2;
        private TextView timetext2;
        private ImageView messageimage2;
        private LinearLayout mlinear2;

        public MyChatViewHolder(View v) {
            super(v);
            mlinear2=(LinearLayout)v.findViewById(R.id.chatincominglinearlayout);
            messagetext2=(TextView)v.findViewById(R.id.incomingmessage);
            timetext2=(TextView)v.findViewById(R.id.incomingmessagetimestamp);
            messageimage2=(ImageView)v.findViewById(R.id.message_image);
        }
    }


    @Override
    public int getItemCount() {
        return mMessageslist.size();
    }

    @Override
    public int getItemViewType(int position) {
        mauth=FirebaseAuth.getInstance();
        user=mauth.getCurrentUser();
        Messages c = mMessageslist.get(position);
        String from_user=c.getFrom();
        if (from_user.equals(user.getUid().toString())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

}
