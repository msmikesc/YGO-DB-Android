package com.example.ygodb;

import android.app.Application;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.utility.YGOLogger;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		AndroidUtil.setAppContext(getApplicationContext());
		Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {

			YGOLogger.logException(paramThrowable);

			// Without System.exit() this will not work.
			System.exit(2);
		});

		AndroidUtil.getDBInstance();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		// The app is about to be terminated, so close the database connection
		AndroidUtil.getDBInstance().closeInstance();
	}
}
