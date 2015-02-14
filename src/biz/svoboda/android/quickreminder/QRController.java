package biz.svoboda.android.quickreminder;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import biz.svoboda.android.utils.MyLogger;

/**
 * Základní aktivita reminderu
 **/
public class QRController extends Activity {
	// TODO: vymazat hodinu/minutu, když se klikne do jejich pole
	// nastavení - defaultní odskok hodin + výchozí text upomínky + výběr zvuku
	// upozornění
	// přestat zvonit po několika opakováních vzonění a po čase zase pokračovat
	// - podívat se na to, proč Time ovladač nebere hodnoty zadané přímo

	Toast mToast;
	EditText alarmText;
	TimePicker timePicker;
	PendingIntent mAlarmSender;
	QRDbAdapter mDbHelper = new QRDbAdapter(this);;
	NotificationManager mNM;

	MyLogger mLogger = new MyLogger(QRController.class);

	public static final String TEXT_KEY = "text_key";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON); // to je tady kvůli
														// ikone okně dialogu
														// (nutné před
														// setContentView)

		setContentView(R.layout.alarm_controller);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.icon_dialog);

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mLogger.Debug("started");

		// zastavení případně běžícího alarmu
		stopAlarm();

		// skrytí případné notifikace
		hideNotification();

		// naplní formulář reminderu
		fillData();

		Button buttonSet = (Button) findViewById(R.id.set_reminder);
		buttonSet.setOnClickListener(mSetReminderListener);

		Button buttonPref = (Button) findViewById(R.id.preferences);
		buttonPref.setOnClickListener(mPreferencesListener);

		Button buttonCancel = (Button) findViewById(R.id.cancel_reminder);
		buttonCancel.setOnClickListener(mCancelReminderListener);

		alarmText.requestFocus();
	}

	private OnClickListener mSetReminderListener = new OnClickListener() {
		public void onClick(View v) {
			mLogger.Debug("started");

			Calendar alarmTime = new GregorianCalendar();
			Integer curHour = alarmTime.get(Calendar.HOUR_OF_DAY);
			Integer curMin = alarmTime.get(Calendar.MINUTE);

			Integer alarmHour = timePicker.getCurrentHour();
			Integer alarmMin = timePicker.getCurrentMinute();

			// if the time is tomorrow
			if ((curHour > alarmHour)
					|| ((curHour == alarmHour) && (curMin > alarmMin))) {
				alarmTime.add(Calendar.HOUR, 24);
			}

			alarmTime.set(Calendar.HOUR_OF_DAY, alarmHour);
			alarmTime.set(Calendar.MINUTE, alarmMin);

			mDbHelper.open();
			// save reminder to the database
			long id = mDbHelper.createReminder(alarmTime.getTimeInMillis(),
					alarmText.getText().toString().trim());

			if (id != -1) {

				startAlarm(alarmTime, id);

				String notificationText = getMyTime(alarmTime) + " - ";

				String t = getText(R.string.empty_reminder_text).toString();

				if (alarmText.getText().toString().trim().length() != 0)
					notificationText = notificationText
							+ alarmText.getText().toString();
				else
					notificationText = notificationText
							+ getText(R.string.empty_reminder_text).toString();

				showNotification(notificationText);

				mDbHelper.close();

				mLogger.Debug(notificationText);
				finish();
			}
			mDbHelper.close();
		}
	};

	private OnClickListener mPreferencesListener = new OnClickListener() {
		public void onClick(View v) {
			// Toast msg = Toast.makeText(QRController.this,
			// "not implemented yet", Toast.LENGTH_SHORT);
			// msg.show();
			startActivity(new Intent(QRController.this, Preferences.class));
		}
	};

	private OnClickListener mCancelReminderListener = new OnClickListener() {
		public void onClick(View v) {
			mLogger.Debug("started");
			finish();
		}
	};

	/**
	 * Naplň formulář události hodnotami z DB
	 */
	private void fillData() {

		alarmText = (EditText) findViewById(R.id.alarmText);
		timePicker = (TimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);

		// načtu si všechny záznamy z DB
		mDbHelper.open();
		Cursor remindersCursor = mDbHelper.fetchAllReminders();
		startManagingCursor(remindersCursor);

		// pokud nejsou žádné záznamy
		if (remindersCursor.getCount() == 0) {
			Calendar settedTime = new GregorianCalendar();
			SharedPreferences prefs = getSharedPreferences(
					Preferences.PREFS_NAME, MODE_PRIVATE);

			settedTime.add(Calendar.MINUTE, prefs
					.getInt(Preferences.INTERVAL_PREF_KEY,
							Preferences.DEFAULT_INTERVAL));
			timePicker.setCurrentHour(settedTime.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(settedTime.get(Calendar.MINUTE));
		} else {

			// pokud tam jsou nějaké záznamy, tak se přesunu na první
			remindersCursor.moveToFirst();

			Reminder r = mDbHelper.negotiateCursor(remindersCursor);
			mLogger.Debug("Reminder loaded from DB");

			Calendar hour = new GregorianCalendar();
			hour.setTimeInMillis(r.getDatetime());
			timePicker.setCurrentHour(hour.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(hour.get(Calendar.MINUTE));

			alarmText.setText(r.getText());

			// smazání záznamu z databáze
			mDbHelper.deleteReminder(r.getId());
		}
		mDbHelper.close();
	}

	/**
	 * Samotné spuštění alarmu
	 * 
	 * @param alarmTime
	 * @param id
	 */
	protected void startAlarm(Calendar alarmTime, long id) {
		// vytvoření intentu s ID nového alarmu
		Intent intent = new Intent(QRController.this, Alert.class);
		intent.putExtra(QRDbAdapter.KEY_ROWID, id);
		mAlarmSender = PendingIntent.getActivity(QRController.this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Schedule the alarm!
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(),
				mAlarmSender);
		mLogger.Debug("Alarm is set");
	}

	/**
	 * Zastavení alarmu
	 */
	protected void stopAlarm() {
		Intent intent = new Intent(QRController.this, Alert.class);
		mAlarmSender = PendingIntent.getActivity(QRController.this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(mAlarmSender);
		mLogger.Debug("Alarm is stopped");
	}

	/**
	 * show the icon in the status bar
	 */
	private void showNotification(String text) {
		// we'll use the same text for the ticker and the
		// expanded notification

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon_dialog,
				text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, QRController.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text,
				contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.notification_started, notification);
		mLogger.Debug("Notification shown");
	}

	// Cancel the notification -- we use the same ID that we had used to
	// start it
	private void hideNotification() {
		mNM.cancel(R.string.notification_started);
		mLogger.Debug("Notification hided");
	}

	private String getMyTime(Calendar cal) {
		String minutes = String.valueOf(cal.get(Calendar.MINUTE));
		if (minutes.length() == 1)
			minutes = "0" + minutes;
		return cal.get(Calendar.HOUR_OF_DAY) + ":" + minutes;
	}
}