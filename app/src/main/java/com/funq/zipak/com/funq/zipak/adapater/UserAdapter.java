package com.funq.zipak.com.funq.zipak.adapater;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.funq.zipak.R;
import com.parse.ParseUser;

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
            //holder.iconImageView = (ImageView)convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            //to scroll the inbox.. else it will crash
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        ParseUser user = mUsers.get(position);

        /*
        if (user.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
            holder.iconImageView.setImageResource(R.drawable.ic_picture);
        }
        else {
            holder.iconImageView.setImageResource(R.drawable.ic_action_play_over_video);
        }*/
        holder.nameLabel.setText(user.getUsername());

        return convertView;
    }

    private static class ViewHolder {
        //ImageView iconImageView;
        TextView nameLabel;
    }

    public void refill(List<ParseUser> users){
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }
}
