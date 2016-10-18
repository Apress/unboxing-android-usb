package com.example.helloandroid;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class myAdapter extends BaseExpandableListAdapter {
	
	private String directory[];
	private String start;
	
	private Context context;
	
	public myAdapter(Context context,String [] directory,String start) {
		
		this.context = context;
		this.directory = directory;
		this.start = start;
		// TODO Auto-generated constructor stub
	}
	
	public void local_values(String [] directory, String start) {
		this.start = start;
		this.directory= directory;
		
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView tv = new TextView(context);
		File f = new File(start + directory[groupPosition]);
		String tvValue = "Nothing";
		if (f.isDirectory()) {
			String temp [] = new String[f.list().length];
			temp = f.list();
			tvValue = temp[childPosition];
		} else {
			try {		
				FileInputStream fs = new FileInputStream(f);
				DataInputStream ds = new DataInputStream(fs);
				tvValue =  ds.readLine();
				ds.close();
				fs.close();
		    }
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		tv.setPadding(70, 0, 0, 0);
		tv.setText(tvValue);
		
		return tv;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		File f = new File(start+directory[groupPosition]);
		if (f.isDirectory())
			return f.list().length;
		else
			return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		
		return groupPosition;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return directory.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView tv = new TextView(context);
		tv.setText(start+directory[groupPosition]);
		tv.setPadding(50, 0, 0, 0);
		return tv;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
}
