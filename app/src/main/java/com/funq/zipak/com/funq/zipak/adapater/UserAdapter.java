package com.funq.zipak.com.funq.zipak.adapater;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.funq.zipak.R;
import com.funq.zipak.com.funq.zipak.utils.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mUsers;

    public UserAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.message_item, users);
        mContext = context;
        mUsers = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent ,false);
            holder = new ViewHolder();
            holder.userImageView = (ImageView)convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            //to scroll the inbox.. else it will crash
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        ParseUser user = mUsers.get(position);
        String email=user.getEmail().toLowerCase();

        if(email.equals("")){
            holder.userImageView.setImageResource(R.drawable.avatar_empty);
        }else{
            String hash= MD5Util.md5Hex(email);
            String gravatarUrl="http://www.gravatar.com/avatar/"+hash+"?s=204&d=404";
            Picasso.with(mContext)
                    .load(gravatarUrl)
                    .placeholder(R.drawable.avatar_empty)
                    .into(holder.userImageView);
        }

        /*
        if (user.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.userImageView.setImageResource(R.drawable.ic_picture);
        }
        else {
            holder.userImageView.setImageResource(R.drawable.ic_action_play_over_video);
        }*/
        holder.nameLabel.setText(user.getUsername());

        return convertView;
    }

    private static class ViewHolder {
        ImageView userImageView;
        TextView nameLabel;
    }

    public void refill(List<ParseUser> users){
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
