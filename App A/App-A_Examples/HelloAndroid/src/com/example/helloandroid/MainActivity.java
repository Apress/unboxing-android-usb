package com.example.helloandroid;

import java.io.File;

import com.example.batterystatusinfo.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

public class MainActivity extends Activity  {

	private File f= new File("/sys/");
	private String []directory;
	private String start = "/sys/";
	private myAdapter adp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		directory = new String[f.list().length];
		directory = f.list();
	//	Toast.makeText(getBaseContext(),directory[2].toString(),2).show();
		ExpandableListView myview = (ExpandableListView)findViewById(R.id.expandableListView1);
		myview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				//Toast.makeText(getBaseContext(),"hello",2).show();
				
				File myfile = new File(start + directory[groupPosition]);
				if(myfile.exists()) {
					if (myfile.isDirectory()) {
						start = start+directory[groupPosition]+"/";
						directory = myfile.list();
						adp.local_values(directory, start);
						adp.notifyDataSetChanged();
						EditText etext = (EditText)findViewById(R.id.editText1);
						etext.setText(start);
					}					
				}
				else
					return false;
				
				//myview.setAdapter(adp);
				return true;
			}
		});
		
		EditText etext = (EditText)findViewById(R.id.editText1);
		etext.setText(start);
		adp = new myAdapter(getBaseContext(), directory, start);
		myview.setAdapter(adp);
		Button myb = (Button)findViewById(R.id.button1);
		myb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText etext = (EditText)findViewById(R.id.editText1);
				File file = new File(etext.getText().toString());
				if (file!= null && file.exists()){
					adp.local_values(file.list(), etext.getText().toString()+"/");
					adp.notifyDataSetChanged();
				}
				else {
					
				}
			}
		});
		}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 //Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	
}
