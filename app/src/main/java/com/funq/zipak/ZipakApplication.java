/**
 * Created by soham on 9/1/14.
 */
package com.funq.zipak;

import android.app.Application;

import com.parse.Parse;

public class ZipakApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "X4ZpfRV0lllBHQnpsuRZbJcxtNpai1IUrLIxjulZ", "U8DgIXOZdEEwRc1bvKMAQwLN5UPNPzpYo2Svz0wg");


    }

}
