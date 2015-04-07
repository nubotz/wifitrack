package com.example.wifitrack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends Activity implements OnMapReadyCallback,
		GoogleMap.OnMapClickListener {
	List<ScanResult> list;
	Timer timer;
	WifiManager wifiManager;
	TextView tv;
	EditText et;
	double lat, longt;
	String location = "";
	GoogleMap map;
	Marker lastMarker;
	ArrayList<Wifi> arrayList = new ArrayList<Wifi>();
	ArrayList<LatLng> markedLocationList = new ArrayList<LatLng>();
	String debug = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		tv = (TextView) findViewById(R.id.tv);
		et = (EditText) findViewById(R.id.et);

		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

	}

	public void onResume() {
		super.onResume();
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		timer = new Timer();
		timer.schedule(new checkWifiThread(), 0, 1000);
	}

	public void onPause() {
		super.onPause();
		timer.cancel();
		wifiManager = null;
	}

	@Override
	public void onMapReady(GoogleMap map) {

		Criteria cri = new Criteria();
		this.map = map;
		try {
			LocationManager locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			String bbb = locationManager.getBestProvider(cri, true);
			Location myLocation = locationManager.getLastKnownLocation(bbb);

			double lat = myLocation.getLatitude();
			double longt = myLocation.getLongitude();
			LatLng currentPosition = new LatLng(lat, longt);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,
					19));
		} catch (Exception e) {
		}
		map.setMyLocationEnabled(true);

		map.setOnMapClickListener(this);

	}

	public void onMapClick(LatLng ll) {
		lat = ll.latitude;
		longt = ll.longitude;

		map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 20));

		Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(
				lat, longt)));
		if (lastMarker != null) {
			lastMarker.remove();
		}
		lastMarker = marker;

		tv.setText("lat: " + lat + "\nlong: " + longt);
	}

	public void onClick(View v) {
		Iterator<Wifi> iterator = arrayList.iterator();

		while (iterator.hasNext()) {
			Wifi o = iterator.next();
			if (o.getSignal() < -50) {
				iterator.remove();
			}
		}
		iterator = arrayList.iterator();
		while (iterator.hasNext()) {
			debug += iterator.next().getSsid() + "\n";
		}
		Log.d("TAG", "MyTag: strongest wifi after remove:" + debug);

		location = et.getText().toString();
		new Thread(new InsertRun()).start();
		NavUtils.navigateUpFromSameTask(this);
	}

	class InsertRun implements Runnable {
		StringBuilder sb;
		int debugNum = 0;

		public void run() {
			try {
				BufferedReader reader = null;
				String data = URLEncoder.encode("location", "UTF-8") + "="
						+ URLEncoder.encode(location, "UTF-8");
				data += "&" + URLEncoder.encode("lat", "UTF-8") + "="
						+ URLEncoder.encode(lat + "", "UTF-8");
				data += "&" + URLEncoder.encode("longt", "UTF-8") + "="
						+ URLEncoder.encode(longt + "", "UTF-8");

				// Defined URL where to send data
				URL url = new URL(
						"http://nubotz.ddns.net/wifi/insertLocation.php");

				// Send POST data request

				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(
						conn.getOutputStream());
				wr.write(data);
				wr.flush();
				wr.close();
				// Thread.sleep(300);

				// ///////////////////

				Iterator<Wifi> i = arrayList.iterator();
				while (i.hasNext()) {
					Wifi wifi = i.next();
					debugNum++;
					data = URLEncoder.encode("location", "UTF-8") + "="
							+ URLEncoder.encode(location, "UTF-8");
					data += "&" + URLEncoder.encode("bssid", "UTF-8") + "="
							+ URLEncoder.encode(wifi.getBssid(), "UTF-8");
					data += "&" + URLEncoder.encode("ssid", "UTF-8") + "="
							+ URLEncoder.encode(wifi.getSsid(), "UTF-8");

					URL url2 = new URL(
							"http://nubotz.ddns.net/wifi/insertWifi.php");

					URLConnection conn2 = url2.openConnection();

					conn2.setDoOutput(true);
					OutputStreamWriter wr2 = new OutputStreamWriter(
							conn2.getOutputStream());
					wr2.write(data);
					wr2.flush();
					wr2.close();

					reader = new BufferedReader(new InputStreamReader(
							conn2.getInputStream()));
					sb = new StringBuilder();
					String line = null;

					// Read Server Response
					while ((line = reader.readLine()) != null) {
						// Append server response in string
						sb.append(line);
					}
					reader.close();
					Log.d("TAG", "MyTag debug" + debug);
					Log.d("TAG", "MyTag" + sb.toString());
					Log.d("TAG", "MyTag debugNum" + debugNum);

					// Thread.sleep(100);

				}
				// Get the server response

				reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				sb = new StringBuilder();
				String line = null;

				// Read Server Response
				while ((line = reader.readLine()) != null) {
					// Append server response in string
					sb.append(line);
				}
				reader.close();
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(),
								sb.toString() + "\ndebugNum" + debugNum,
								Toast.LENGTH_LONG).show();
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private class Wifi {
		private int signal;
		private String bssid;
		private String ssid;

		public Wifi(String b, String s) {
			bssid = b;
			ssid = s;
		}

		public void addSignal(int s) {
			signal = (signal + s) / 2;
		}

		public int getSignal() {
			return signal;
		}

		public String getBssid() {
			return bssid;
		}

		public String getSsid() {
			return ssid;
		}
	}

	class checkWifiThread extends TimerTask {
		ScanResult sr;
		LatLng gotLL;
		String parts[];

		@Override
		public void run() {

			wifiManager.startScan();
			// TODO Auto-generated method stub
			list = wifiManager.getScanResults();
			Iterator<ScanResult> i = list.iterator();

			for (int x = 0; x < 5 && i.hasNext(); x++) {
				sr = i.next();

				Iterator<Wifi> iterator = arrayList.iterator();
				boolean existed = false;
				while (iterator.hasNext()) {
					Wifi wifi = iterator.next();
					if (wifi.getBssid().equals(sr.BSSID)) {
						wifi.addSignal(sr.level);
						existed = true;
					}
				}
				if (!existed) {
					Wifi currentWifi = new Wifi(sr.BSSID, sr.SSID);
					arrayList.add(currentWifi);
					currentWifi.addSignal(sr.level);
				}

				try {
					String data = URLEncoder.encode("bssid", "UTF-8") + "="
							+ URLEncoder.encode(sr.BSSID, "UTF-8");

					BufferedReader reader = null;

					// Defined URL where to send data
					URL url = new URL("http://nubotz.ddns.net/wifi/query.php");

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
					String text = sb.toString();
					Log.d("TAG", "MyTAG, responde=" + text);
					if (!text.startsWith("no")) {

						parts = text.split(",");

						gotLL = new LatLng(Double.parseDouble(parts[1]),
								Double.parseDouble(parts[2]));

						Iterator<LatLng> locaIter = markedLocationList
								.iterator();
						boolean found = false;
						while (locaIter.hasNext()) {
							if (gotLL.equals(locaIter.next())) {
								found = true;
							}
						}

						if (!found) {

							runOnUiThread(new Runnable() {
								public void run() {
									map.addMarker(new MarkerOptions().position(
											gotLL).title(parts[0]));
									markedLocationList.add(gotLL);
								}
							});
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
