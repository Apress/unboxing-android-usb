package com.example.my_batteryinfo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class My_BatteryInfo extends Activity {

	TextView textview;
	ProgressBar progressbar;
	Handler h = new Handler();
	Runnable myrunnable;
	float batteryPct;
	int level;
	int scale;
	IntentFilter ifilter;
	Intent batteryStatus;
	String final_string;
	boolean oncreate;
	
	int time = 2; //in  seconds
    int sampleRate = 8000;
    int numberofSamples = time * sampleRate;
    double sample[] = new double[numberofSamples];
    double freqOfTone = 500; //in hz

    byte generatedSound[] = new byte[2 * numberofSamples];
    private AudioTrack audioTrack;
    static int temp = 0;
    Handler handler = new Handler();

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my__battery_info);
		textview =(TextView)findViewById(R.id.mytextview);
		progressbar = (ProgressBar)findViewById(R.id.progressBar1);
		progressbar.setVisibility(ProgressBar.VISIBLE);
		generateTone();
		myrunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				batteryStatus = registerReceiver(null, ifilter);
				level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				batteryPct = (level / (float)scale) * 100;
				int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				int type = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);
	
				final_string = "Charging Battery.\n";
				if (oncreate) {
					progressbar.setProgress((int)batteryPct);
					oncreate = false;
				}
				if (isCharging) {
					if (type == BatteryManager.BATTERY_PLUGGED_AC)
						final_string = final_string + "AC Charger Plugged\n";
					if (type == BatteryManager.BATTERY_PLUGGED_USB)
						final_string = final_string + "USB Charger Plugged\n";
					textview.setText( final_string + "Battery % = " + batteryPct);
					if (progressbar.getProgress() <= 99) {
						progressbar.incrementProgressBy(1);
					} else {
						progressbar.setProgress((int)batteryPct);
						generateTone();
						if (temp < 10) {
							if ((temp & 1) == 1)
								audioTrack.play();
							else
								audioTrack.stop();
							temp++;
						}
					}
				} else {
					temp = 0;
					textview.setText("Dis-" + final_string + "Battery % = " + batteryPct);
					progressbar.setProgress((int)batteryPct);
				}
				h.postDelayed(myrunnable, 1000);
			}
		};
		h.postDelayed(myrunnable, 1000);
		oncreate = true;
		
	}
	void generateTone(){
        // fill out the array
        for (int i = 0; i < numberofSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numberofSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSound, 0, generatedSound.length);
    }


}
