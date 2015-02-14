package biz.svoboda.android.utils;

import android.util.Log;

/**
 * @author Kamil Svoboda
 * 
 */
public class MyLogger {
	Class loggedClass;

	public MyLogger(Class c) {
		loggedClass = c;
	}

	public void Debug(String message) {
		Log.d(loggedClass.getSimpleName(), Thread.currentThread()
				.getStackTrace()[3].getMethodName() + " - " + message);
	}

	public void Error(Exception e) {
		Log.e(loggedClass.getSimpleName(), Thread.currentThread()
				.getStackTrace()[3].getMethodName() + " - " + e.getMessage(), e);
	}

	public void Error(String errorMessage) {
		Log.e(loggedClass.getSimpleName(), Thread.currentThread()
				.getStackTrace()[3].getMethodName() + " - " + errorMessage);
	}
}
