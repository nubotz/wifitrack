package com.example.wifitrack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	List<ScanResult> list;
	TextView tv, tv2;
	EditText et;
	WifiManager wifiManager;
	Timer timer;
	String bssid_best = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.tv);
		tv2 = (TextView) findViewById(R.id.tv2);
		et = (EditText) findViewById(R.id.et);
		timer = new Timer();

	}
	public void onResume(){
		super.onResume();
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		timer.schedule(new Thread(), 0, 2000);
	}

	public void onPause() {
		super.onPause();
		timer.cancel();
		wifiManager = null;
	}

	public void onClick(View v) {
		Intent intent = new Intent(this, Map.class);
		startActivity(intent);
	}

	class Thread extends TimerTask {
		ScanResult sr;
		StringBuilder sb;
		String text = "";
		@Override
		public void run() {
			wifiManager.startScan();
			// TODO Auto-generated method stub
			sb = new StringBuilder();
			List<ScanResult> list = wifiManager.getScanResults();
			Iterator<ScanResult> i = list.iterator();
			
			sb.append("time: "+System.currentTimeMillis());
			sb.append("Scanned best 5 result:\n");
			for (int x = 0; x < 5 && i.hasNext(); x++) {
				sr = i.next();
				//if (sr.level < -50) {
				if(x == 0)//strongest signal level come first
					bssid_best = sr.BSSID;
				//}
				sb.append("\n" + "SSID: " + sr.SSID + "\tMAC: " + sr.BSSID+ "\tlevel: " + sr.level);

			}
			runOnUiThread(new Runnable() {
				public void run() {
					tv.setText(sb.toString());
				}
			});
			
			
			try {
				String data = URLEncoder.encode("bssid", "UTF-8") + "="
						+ URLEncoder.encode(bssid_best, "UTF-8");
				Log.d("TAG","MyTAG:bssid_best is "+bssid_best);
				
				BufferedReader reader = null;
				try {

					// Defined URL where to send data
					URL url = new URL(
							"http://nubotz.ddns.net/wifi/query.php");

					// Send POST data request

					URLConnection conn = url.openConnection();
					conn.setDoOutput(true);
					OutputStreamWriter wr = new OutputStreamWriter(
							conn.getOutputStream());
					wr.write(data);
					wr.flush();
					wr.close();
					// Get the server response

					reader = new BufferedReader(new InputStreamReader(
							conn.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line = null;

					// Read Server Response
					while ((line = reader.readLine()) != null) {
						// Append server response in string
						sb.append(line + "\n");
					}
					reader.close();
					text = sb.toString();
				 

				}catch (Exception ex) {
				}

				// Show response on activity
				runOnUiThread(new Runnable() {
					public void run() {
						if(!text.isEmpty()){
						String parts[] = text.split(",");
						tv2.setText("\n\n\tYou are at " +parts[0]);}
					}
				});
				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
