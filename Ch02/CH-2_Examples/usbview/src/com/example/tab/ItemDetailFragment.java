package com.example.tab;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tab.dummy.DummyContent;
import com.example.tab.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}
	private static String hex(int n) {
		return String.format("0x%4s", Integer.toHexString(n)).replace(" ", "0");
	}
	private static String hex2(int n) {
		return String.format("0x%2s", Integer.toHexString(n)).replace(" ", "0");
	}
	private static int b2i(byte temp) {
		return temp & 0xFF;
	}
	private static StringBuilder deviceDescriptor(DummyItem mItem,byte[] buffer,StringBuilder sb)
	{
		sb.append("Device Descriptor:\n\n");
		int lower = b2i(buffer[2]);
		int temp = lower & 0x0F;
		lower = (lower >> 4) | (temp << 4);
		sb.append("bcdUSB : " + b2i(buffer[3]) +"." + lower +"\n");
		sb.append("bDeviceClass : ");
		temp = b2i(buffer[4]);
		if (temp == 0)
			sb.append("Defined at Interface level\n" );
		else
			sb.append(hex2(temp)+"\n");
		sb.append("bDeviceSubClass : ");
		temp = b2i(buffer[5]);
		sb.append(hex2(temp)+"\n");
		sb.append("bDeviceProtocol : ");
		temp = b2i(buffer[6]);
		if (temp==0)
			sb.append("none\n");
		 else
			 sb.append(hex2(temp)+"\n");
		 sb.append("bMaxPacketSize0 : ");
		 temp = b2i(buffer[7]);
		 sb.append(hex2(temp)+ "(" + temp + ")"+"\n");
		 sb.append("idVendor : " + hex(mItem.device.getVendorId()) +"\n");
		 sb.append("idProduct : " + hex(mItem.device.getProductId())+ "\n");
		 sb.append("bcdDevice: ");
		 temp = b2i(buffer[13]);
		 sb.append(temp+".");
		 temp = b2i(buffer[12]);
		 lower = temp & 0x0F;
		 temp = temp & 0xF0;
		 temp = temp >> 4;
		 sb.append(temp+"."+lower+"\n");
		 sb.append("iManufacturer : ");
		 temp = b2i(buffer[14]);
		 sb.append(hex2(temp)+"\n");
		 sb.append("iProduct : ");
		 temp = b2i(buffer[15]);
		 sb.append(hex2(temp)+"\n");
		 temp = b2i(buffer[16]);
		 sb.append("iSerialNumber : ");
		 sb.append(hex2(temp)+"\n");
		 temp = b2i(buffer[17]);
		 mItem.numConfigerations = temp;
		 sb.append("bNumConfigerations : ");
		 sb.append(hex2(temp)+"\n\n");
		 return sb;
	}
	private static StringBuilder miniConfigurationDescriptor(DummyItem mItem,byte[] buffer,StringBuilder sb) {
		
		int temp, lower;
		sb.append("\nConfiguration Descriptor:\n\n");
		sb.append("wTotalLength : ");
		temp = b2i(buffer[3]);
		lower = b2i(buffer[2]);
		temp = temp << 8;
		temp = temp | lower;
		mItem.totalLength = temp;
		sb.append(hex(temp)+ "("+ temp +" bytes)"+ "\n");
		sb.append("bNumInterfaces : ");
		temp = b2i(buffer[4]);
		sb.append(hex2(temp) + "\n");
		sb.append("bConfigurationValue : ");
		temp = b2i(buffer[5]);
		sb.append(hex2(temp)+"\n");
		sb.append("iConfiguration : ");
		temp = b2i(buffer[6]);
		sb.append(hex2(temp)+ "\n");
		sb.append("bmAttributes : ");
		temp = b2i(buffer[7]);
		sb.append(hex2(temp)+"\n");
		sb.append("MaxPower : ");
		temp = b2i(buffer[8]);
		sb.append(hex2(temp) + " ("+ temp*2+"mA)"+ "\n");
		return sb;
	}
	private static String decode_class(int temp) {
		
		switch (temp) {
			case 1 : return "(Audio)"; 	
			case 8 : return "(Mass Storage)";
		}
		return null;
	}
	private static StringBuilder interfaceDescriptor(DummyItem mItem,byte[] buffer,StringBuilder sb,int start,int length) {
		
		int temp;
		sb.append("\nInterface Descriptor :\n\n");
		temp = b2i(buffer[start]);
		sb.append("bIterfaceNumber :"+ hex2(temp)+"\n");
		temp = b2i(buffer[start+1]);
		sb.append("bAlternateSettings : "+hex2(temp)+"\n");
		temp = b2i(buffer[start+2]);
		sb.append("bNumEndpoints : "+ hex2(temp)+"\n");
		temp = b2i(buffer[start+3]);
		sb.append("bInterfaceClass : "+ hex2(temp)+ decode_class(temp)+"\n");
		temp = b2i(buffer[start+4]);
		sb.append("bInterfaceSubClass : "+hex2(temp)+"\n");
		temp = b2i(buffer[start+5]);
		sb.append("bInterfaceProtocol : " + hex2(temp)+"\n");
		temp = b2i(buffer[start+6]);
		sb.append("iInterface : "+ hex2(temp)+ "\n");
		return sb;
	}
	
	private static String decode_trasfer(int temp)
	{
		temp = temp & 3;
		switch (temp) {
			case 0 : return "Control";
			case 1 : return "Isocronous";
			case 2 : return "Bulk";
			case 3 : return "Interrupt"; 
		} 
		return null;
	}
	private static StringBuilder endPointDescriptor(DummyItem mItem,byte[] buffer,StringBuilder sb,int start,int length) {
		
		int temp,lower;
		sb.append("\nEndpoint Descriptor :\n\n");
		temp = b2i(buffer[start]);
		sb.append("bEndpoint Address : "+hex2(temp)+"\n");
		temp = b2i(buffer[start+1]);
		sb.append("TrasferType :"+ decode_trasfer(temp)+"\n");
		temp = b2i(buffer[start+2]);
		lower = b2i(buffer[start+3]);
		temp = temp << 8;
		temp = temp | lower;
		sb.append("wMaxPacketSize : "+ hex(temp) +"\n" );
		temp = b2i(buffer[start+4]);
		sb.append("bInterval : "+ hex2(temp)+ "\n");
		return sb;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);
		// Show the dummy content as text in a TextView.
				if (mItem != null) {
					if (!(mItem.content == "Nothing") && (mItem.device != null)) {
						int ret=0;
						int local,temp;
						UsbManager manager = 
								(UsbManager)getActivity().getSystemService(Context.USB_SERVICE);
						StringBuilder sb = new StringBuilder();
						UsbDeviceConnection connection = manager.openDevice(mItem.device);
			        	byte[] buffer = new byte[255];
			        	//Toast.makeText(getActivity().getBaseContext(),"1",2).show();
						try {
							/*Get Device Descriptor*/
							 ret = connection.controlTransfer(0x80, 0x06, 0x0100, 0,buffer, 18, 5000);
							 /* Parse device Descriptor */
							 sb = deviceDescriptor(mItem, buffer, sb);
							 /*Configuration Descriptor */
							 ret = connection.controlTransfer(0x80, 0x06, 0x0200, 0,buffer, 9, 5000);
							 sb = miniConfigurationDescriptor(mItem, buffer, sb);
							 ret = connection.controlTransfer(0x80, 0x06, 0x0200, 0,buffer, 255, 5000);
							 //Toast.makeText(getActivity().getBaseContext(),""+ret,2).show();
							 local = 9;
							 while (local < ret ) {
								 temp = b2i(buffer[local+1]);
								 if (temp == 4)
									sb = interfaceDescriptor(mItem, buffer, sb, local+2, b2i(buffer[local+1]));
								 if (temp == 5)
									sb = endPointDescriptor(mItem, buffer, sb, local+2, b2i(buffer[local+1]));
								 local = local + b2i(buffer[local]);
								// Toast.makeText(getActivity().getBaseContext(),""+local,2).show();
							 }
						} 
						catch(Exception e){
							//Toast.makeText(getActivity().getBaseContext(),"2",2).show();
							e.printStackTrace();
						}
						((TextView) rootView.findViewById(R.id.item_detail))
							.setText(sb.toString());
					
				//	((TextView) rootView.findViewById(R.id.item_detail))
					//.append("\nSaketh");
					} else {
						((TextView) rootView.findViewById(R.id.item_detail))
						.setText("No details can be found as nothing is connected");
					}
					
				} 
		return rootView;
	}
}
