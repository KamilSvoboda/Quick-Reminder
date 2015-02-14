package biz.svoboda.android.quickreminder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import biz.svoboda.android.utils.MyLogger;

/**
 * @author Kamil Svoboda
 * 
 */
public class Preferences extends Activity {
	MyLogger mLogger = new MyLogger(Preferences.class);
	SharedPreferences prefs;
	EditText intervalText;
	public static final String PREFS_NAME = "QRPreferences";
	public static final String INTERVAL_PREF_KEY = "interval";
	public static final int DEFAULT_INTERVAL = 15;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_controller);
		mLogger.Debug("started");

		prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		int intervalPref = prefs.getInt(INTERVAL_PREF_KEY, DEFAULT_INTERVAL);

		intervalText = (EditText) findViewById(R.id.intervalEditText);
		intervalText.setText(String.valueOf(intervalPref));

		Button buttonAddToInterval = (Button) findViewById(R.id.add_to_interval);
		buttonAddToInterval.setOnClickListener(mAddToInterval);
		
		Button buttonRemFromInterval = (Button) findViewById(R.id.rem_from_interval);
		buttonRemFromInterval.setOnClickListener(mRemFromInterval);
		
		Button buttonPrefOk = (Button) findViewById(R.id.pref_ok);
		buttonPrefOk.setOnClickListener(mPrefOk);
	}

	OnClickListener mAddToInterval = new OnClickListener() {
		public void onClick(View v) {
			int i = Integer.valueOf(intervalText.getText().toString());
			intervalText.setText(String.valueOf(++i));
		};
	};
	
	OnClickListener mRemFromInterval = new OnClickListener() {
		public void onClick(View v) {
			int i = Integer.valueOf(intervalText.getText().toString());
			intervalText.setText(String.valueOf(--i));
		};
	};

	OnClickListener mPrefOk = new OnClickListener() {
		public void onClick(View v) {
			mLogger.Debug("Settings saved");
			SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putInt(INTERVAL_PREF_KEY,
					Integer.valueOf(intervalText.getText().toString()));
			prefEditor.commit();
			finish();
		}
	};
}
