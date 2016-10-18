package com.example.mtp_massstorage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.app.AlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;


public class MainActivity extends Activity {

	Button mtp_to_msc_button;
	Button msc_to_mtp_button;
	Button default_config;
	String default_Configeration;
	boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		  if (!RootTools.isRootAvailable()) {
      		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
      		.setTitle("MTP_UMS")
      		.setMessage("MTP_UMS")
      		.setPositiveButton("MTP_UMS", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog,int which) {
                  	dialog.dismiss();
                  	finish();
                  }
              });

		flag = false;


	}
		    mtp_to_msc_button = (Button) findViewById(R.id.mtp_to_msc_button);
			msc_to_mtp_button = (Button) findViewById(R.id.msc_to_mtp_button);

			addListenersToButtons();
	}
	 public boolean execCommandAsSU(String... commandString) {

		 Toast.makeText(getBaseContext(),"Run command",Toast.LENGTH_LONG).show();
	    	CommandCapture command = new CommandCapture(0, commandString);
	    	try {
				RootTools.getShell(true).add(command).wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	return true;
	    }
	public void addListenersToButtons() {

    	OnClickListener mtp2ums = new OnClickListener() {

			public void onClick(View arg0) {
			// TODO Auto-generated method stub
				//String enable_path = "/sys/class/android_usb/android0/enable";
				//String functions_path = "/sys/class/android_usb/android0/functions";

				Toast.makeText(getBaseContext(),"MTP MSC Click",Toast.LENGTH_LONG).show();

				execCommandAsSU("echo 0 > /sys/class/android_usb/android0/enable");
				execCommandAsSU("echo mass_storage,adb > /sys/class/android_usb/android0/functions");
				execCommandAsSU("echo /dev/block/mmcblk1 > /sys/class/android_usb/android0/f_mass_storage/lun/file");
				execCommandAsSU("echo 1 > /sys/class/android_usb/android0/enable");
				execCommandAsSU("setprop sys.usb.state mass_storage,adb");

			};
    	};
    	mtp_to_msc_button.setOnClickListener(mtp2ums);

    	OnClickListener ums2mtp= new OnClickListener() {

			public void onClick(View arg0) {
			// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),"MSC MTP Click",Toast.LENGTH_LONG).show();

				execCommandAsSU("echo 0 > /sys/class/android_usb/android0/enable");

				execCommandAsSU("echo mtp,adb > /sys/class/android_usb/android0/functions");
				execCommandAsSU("echo 1 > /sys/class/android_usb/android0/enable");
				execCommandAsSU("setprop sys.usb.state mtp,adb");

			};
    	};
    	msc_to_mtp_button.setOnClickListener(ums2mtp);


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
