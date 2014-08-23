package com.example.androidapp7;

import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationManager locationManager = null;	
	private EditText latText  = null;
	private EditText lonText = null;
	private TextView resText = null;
	private String latitude = " ";
	private String longitude = " ";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("最寄駅検索");
		latText = (EditText) findViewById(R.id.editText1);
		lonText = (EditText) findViewById(R.id.editText2);
		resText = (TextView) findViewById(R.id.textView3);
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(mButton1Listener);
		Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(mButton2Listener);
	}
//	GPSで駅検索
	private OnClickListener mButton2Listener = new OnClickListener() {
		public void onClick(View v) {
	        if (locationManager != null) {
	        	// 取得処理を終了
	        	locationManager.removeUpdates(mLocationListener);
	        }
        	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        	
            // GPSから位置情報を取得する設定
            boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        	// 3Gまたはwifiから位置情報を取得する設定
            boolean isWifiOn =  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            String provider = null;
			if (isGpsOn) {
				provider = LocationManager.GPS_PROVIDER;
			} else if (isWifiOn) {
            	provider = LocationManager.NETWORK_PROVIDER;
            } else {
            	Toast.makeText(getApplicationContext(), "Wi-FiかGPSをONにしてください", Toast.LENGTH_LONG).show();
            	return;
            }
			Toast.makeText(getApplicationContext(), "Provider=" + provider, Toast.LENGTH_LONG).show();
			
			// ロケーション取得を開始
            locationManager.requestLocationUpdates(provider, 1000L, 0, mLocationListener);
        }	
	};
	
	private LocationListener mLocationListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
        public void onLocationChanged(Location location) {
        	latitude = Double.toString(location.getLatitude());
        	longitude = Double.toString(location.getLongitude());        
            // 位置情報の取得を1回しか行わないので取得をストップ
            locationManager.removeUpdates(mLocationListener);
            String requestURL = "http://map.simpleapi.net/stationapi?y="
					+ latitude + "&x=" + longitude +"&output=json";
			resText.setText(requestURL);
			Log.d("REQUEST_URL", requestURL);
    		Toast.makeText(getApplicationContext(), requestURL, Toast.LENGTH_LONG).show();
			Task task = new Task();
	        task.execute(requestURL);
        }    
	};
  	
	private OnClickListener mButton1Listener = new OnClickListener() {
		public void onClick(View v) {
				latitude = latText.getText().toString();
				longitude= lonText.getText().toString();
			String requestURL = "http://map.simpleapi.net/stationapi?y="
					+ latitude + "&x=" + longitude +"&output=json";
			resText.setText(requestURL);
			Log.d("REQUEST_URL", requestURL);
    		Toast.makeText(getApplicationContext(), requestURL, Toast.LENGTH_LONG).show();
			Task task = new Task();
	        task.execute(requestURL);
		}
	};
	
	protected class Task extends AsyncTask<String, String, String>
	{
        @Override
        protected String doInBackground(String... params)
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(params[0]);
            String rtn = "";
            try{
                HttpResponse response = client.execute(get);
                Log.d("EXECUTE", "Executed!!");
                
                StatusLine statusLine = response.getStatusLine();
                Log.d("STATUS_LINE", "StatusCode = " + statusLine.getStatusCode());
                
                if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                	byte[] result = EntityUtils.toByteArray(response.getEntity());
                    rtn = new String(result, "UTF-8");
                }
            } catch (Exception e) {
        		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            Log.d("RETURN", rtn);
            return rtn;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
			try {
				// JSONArrayがエントリのため、これをしないと例外で落ちる
				String jsonBase = "{\"root\":" + result + "}";
				JSONObject json = new JSONObject(jsonBase);
				Log.d("JSON_STRING", json.toString());
				JSONObject obj = json.getJSONArray("root").getJSONObject(0);
				String name = obj.getString("name");
				resText.setText(name);
			} catch (JSONException e) {
    			resText.setText("Json Error!!!" + e.getMessage());
                e.printStackTrace();
            }
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
