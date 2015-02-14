package biz.svoboda.android.quickreminder;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import biz.svoboda.android.utils.MyLogger;

/**
 * @author Kamil Svoboda
 * 
 */
public class Alert extends Activity {
	MyLogger mLogger = new MyLogger(Alert.class);

	Toast mToast;
	Long mReminderId;
	String mReminderText;
	MediaPlayer mMediaPlayer;
	QRDbAdapter mDbAdapter = new QRDbAdapter(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLogger.Debug("started");
		mReminderId = (Long) getIntent().getLongExtra(QRDbAdapter.KEY_ROWID,
				new Long(-1));

		if (mReminderId != -1) {
			// vytáhnu si z databáze záznam připomínky
			mDbAdapter.open();
			Cursor cursor = mDbAdapter.fetchReminder(mReminderId);
			startManagingCursor(cursor);
			Reminder r = new Reminder();
			r = mDbAdapter.negotiateCursor(cursor);
			mReminderText = r.getText();
			mDbAdapter.close();

		} else {
			mLogger.Error("Reminder wasn't found in DB!");
			mReminderText = getText(R.string.empty_reminder_text).toString();
		}

		if (mReminderText == null || mReminderText.toString().trim().length() == 0)
			mReminderText = getText(R.string.empty_reminder_text).toString();
		
		hideNotification();

		showDialog(R.string.alert_close);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		mLogger.Debug("started");

		try {
			Uri alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(this, alert);
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (IllegalStateException e) {
			mLogger.Error(e);
		} catch (IOException e) {
			mLogger.Error(e);
		}

		AlertDialog dialog = new AlertDialog.Builder(Alert.this).create();
		dialog.setIcon(R.drawable.icon_dialog);
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(mReminderText);
		dialog.setCancelable(false);
		dialog.setButton(Dialog.BUTTON1, getText(R.string.alert_reset),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						shutUp();
						// spustim contoller pro nové natavení budíku
						Intent intent = new Intent(Alert.this,
								QRController.class);
						startActivity(intent);

						Alert.this.finish();
					}
				});
		dialog.setButton(Dialog.BUTTON2, getText(R.string.alert_close),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						shutUp();
						// smažu připomínku z DB
						if (mReminderId != -1) {
							mDbAdapter.open();
							mDbAdapter.deleteReminder(mReminderId);
							mDbAdapter.close();
						}

						Alert.this.finish();
					}
				});
		return dialog;
	}

	// vypne zvonění
	private void shutUp() {
		if (Alert.this.mMediaPlayer != null
				&& Alert.this.mMediaPlayer.isPlaying())
			Alert.this.mMediaPlayer.stop();

		Alert.this.mMediaPlayer.release();
	}

	// Cancel the notification -- we use the same ID that we had used to
	// start it
	private void hideNotification() {
		NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNM.cancel(R.string.notification_started);
		mLogger.Debug("Notification hided");
	}
}
