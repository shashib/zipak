/**
 * Created by soham on 9/1/14.
 */
package com.funq.zipak;

import android.app.Application;

import com.funq.zipak.com.funq.zipak.ui.MainActivity;
import com.funq.zipak.com.funq.zipak.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

public class ZipakApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "X4ZpfRV0lllBHQnpsuRZbJcxtNpai1IUrLIxjulZ", "U8DgIXOZdEEwRc1bvKMAQwLN5UPNPzpYo2Svz0wg");

        //PushService.setDefaultPushCallback(this, MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class,R.drawable.ic_stat_z);
        ParseInstallation.getCurrentInstallation().saveInBackground();

    }

    public static void updateParseInstallation(ParseUser user){
        ParseInstallation installationtion= ParseInstallation.getCurrentInstallation();
        installationtion.put(ParseConstants.KEY_USER_ID,user.getObjectId());
        installationtion.saveInBackground();
    }

}
