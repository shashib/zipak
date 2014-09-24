package com.funq.zipak.com.funq.zipak.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.funq.zipak.R;
import com.funq.zipak.com.funq.zipak.adapater.UserAdapter;
import com.funq.zipak.com.funq.zipak.utils.FileHelper;
import com.funq.zipak.com.funq.zipak.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RecipientsActivity extends Activity {
    public static final String TAG= RecipientsActivity.class.getSimpleName();

    protected ParseUser mCurrentUser;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected List<ParseUser> mFriends;

    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;

    protected GridView mGridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        //to select multiple items in list

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        mGridView=(GridView)findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView=(TextView)findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

    }

    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseUser> query=mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e==null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                            getListView().getContext(),
//                            android.R.layout.simple_list_item_checked,
//                            usernames);
//
                    if(mGridView.getAdapter()==null) {
                        UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                        mGridView.setAdapter(adapter);
                    }else{
                        ((UserAdapter)mGridView.getAdapter()).refill(mFriends);
                    }

                }else{
                    //error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder  builder= new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage());
                    builder.setTitle(R.string.error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);
        //to get the menu items
        mSendMenuItem=menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()){
            case R.id.action_settings:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_send:
                //  to send
                //1. create a parse object
                //2. Upload it to backend
                ParseObject message= createMessage();
                if(message==null){
                    //error
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.error_selecting_file)).setTitle(getString(R.string.error_selecting_file_title)).setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                send(message);
                //to return to main acitivity which launch this activity
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    //message sent
                    Toast.makeText(RecipientsActivity.this, getString(R.string.success_message),Toast.LENGTH_LONG).show();
                    sendPushNotification();
                }else{
                    //error
                    AlertDialog.Builder builder=new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(getString(R.string.error_sending_message)).setTitle(getString(R.string.error_selecting_file_title)).setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    protected void sendPushNotification() {
        ParseQuery<ParseInstallation> query=ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        //send push notification
        ParsePush push= new ParsePush();
            push.setQuery(query);
        push.setMessage(getString(R.string.push_notification,
                                    ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }

    private ParseObject createMessage() {
        ParseObject message=new ParseObject(ParseConstants.CLASS_MESSAGE);
        message.put(ParseConstants.KEY_SENDER_ID,ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME,ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE,mFileType);

        byte[] fileBytes= FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes ==null){
            return null;
        }else{
            //create Parse file
            if(mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes=FileHelper.reduceImageForUpload(fileBytes);
            }
            String fileName=FileHelper.getFileName(this,mMediaUri,mFileType);

            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE,file);
        }
        return message;
    }

    private ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds=new ArrayList<String>();
        for(int i=0;i<mGridView.getCount();i++){
            if(mGridView.isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            if(mGridView.getCheckedItemCount()>0){
                mSendMenuItem.setVisible(true);
            }else{
                mSendMenuItem.setVisible(false);
            }

            if (mGridView.isItemChecked(position)) {
                //add recipients
                checkImageView.setVisibility(view.VISIBLE);

            } else {
                //remove recipients
                checkImageView.setVisibility(view.INVISIBLE);
            }

            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

        }
    };
}
