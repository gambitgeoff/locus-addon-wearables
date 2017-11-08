package com.asamm.locus.addon.wear;

import android.app.Application;
import android.util.Log;

import com.asamm.locus.addon.wear.gui.CustomActivityOld;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import locus.api.utils.Logger;

/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
public class MainApplicationOld extends Application {

    // tag for logger
    private static final String TAG = "MainApplicationOld";

    @Override
    public void onCreate() {
        super.onCreate();

        // set logger
        Logger.registerLogger(new Logger.ILogger() {

            @Override
            public void logI(String tag, String msg) {
                Log.i(tag, msg);
            }

            @Override
            public void logD(String tag, String msg) {
                Log.d(tag, msg);
            }

            @Override
            public void logW(String tag, String msg) {
                Log.w(tag, msg);
            }

            @Override
            public void logE(String tag, String msg) {
                Log.e(tag, msg);
            }

            @Override
            public void logE(String tag, String msg, Exception e) {
                Log.e(tag, msg, e);
            }
        });

        // notify about create of app
        Logger.logE(TAG, "onCreate()");
    }

    /**
     * Destroy instance of this application.
     */
    public void onDestroy() {
        Logger.logE(TAG, "onDestroy()");
        // destroy instance of communication class
        DeviceCommunicationOld.destroyInstance();
    }

    /**************************************************/
    // STATE OF CURRENT ACTIVITY
    /**************************************************/

    /**
     * Called when activity move to "onCreate" state.
     * @param act current activity
     */
    public static void activityOnCreate(CustomActivityOld act) {
        initializeDeviceCommunication(act);
    }

    /**
     * Called when activity move to "onStart" state.
     * @param act current activity
     */
    public static void activityOnStart(CustomActivityOld act) {
        initializeDeviceCommunication(act);

        // register activity
        DeviceCommunicationOld.getInstance().checkConnection(act);
    }

    /**
     * Called when activity move to "onResume" state.
     * @param act current activity
     */
    public static void activityOnResume(CustomActivityOld act) {
        // set current activity
        CustomActivityOld oldAct = getCurrentActivity();
        if (oldAct == null || oldAct == act) {
            // just set current activity, for sure
            setCurrentActivity(act);
        } else {
            // check state of old custom activity
            if (oldAct.getCurrentState() == CustomActivityOld.State.ON_START ||
                    oldAct.getCurrentState() == CustomActivityOld.State.ON_PAUSE ||
                    oldAct.getCurrentState() == CustomActivityOld.State.ON_STOP) {
                setCurrentActivity(act);
            }
        }
    }

    /**
     * Called when activity move to "onStop" state.
     * @param act current activity
     */
    public static void activityOnStop(CustomActivityOld act) {
        // activity is not visible
        if (getCurrentActivity() == act) {
            setCurrentActivity(null);
        }
    }

    /**
     * Initialize instance of device communication if not yet exists.
     * @param act current activity
     */
    private static void initializeDeviceCommunication(CustomActivityOld act) {
        // recreate communication class
        if (DeviceCommunicationOld.getInstance() == null) {
            // get reference to application
            final MainApplicationOld app = (MainApplicationOld)
                    act.getApplication();

            // create instance of "device"
            DeviceCommunicationOld.initialize(app);
        }
    }

    /**************************************************/
    // APPLICATION REFERENCE
    /**************************************************/

    // reference to current activity
    private static CustomActivityOld mCurrentActivity;
// TODO cejnar currently not used, delete if not used for activity saving/restoring
    private static boolean mIsLastActivityNull = true;

    // timer for termination
    private static Timer mTimerTerminate;

    /**
     * Check if any activity is still active (registered).
     * @return <code>true</code> if any activity is still registered
     */
    public static boolean existCurrentActivity() {
        return mCurrentActivity != null;
    }

    /**
     * Get current registered activity. In case, no activity is active, return at
     * least main activity of whole application.
     * @return current activity
     */
    public static CustomActivityOld getCurrentActivity() {
        return mCurrentActivity;
    }

    /**
     * Set reference to current activity.
     * @param act current activity
     */
    private static void setCurrentActivity(CustomActivityOld act) {
        Logger.logD(TAG, "setCurrentActivity(" + act + ")");

        // if new activity is registered, end timer
        if (act != null && mTimerTerminate != null) {
            mTimerTerminate.cancel();
            mTimerTerminate = null;
        }
        // register activity
        if (mCurrentActivity == null && act != null) {
            Logger.logW(TAG, " - application restored");
        } else if (mCurrentActivity != null && act == null) {
            Logger.logW(TAG, " - application terminated");

            // get reference to application
            final MainApplicationOld app = (MainApplicationOld)
                    mCurrentActivity.getApplication();

            // start timer
            TimerTask terminateTask = new TimerTask() {

                @Override
                public void run() {
                    app.onDestroy();
                }
            };

            // execute timer
            mTimerTerminate = new Timer();
            mTimerTerminate.schedule(terminateTask,
                    TimeUnit.SECONDS.toMillis(10));
        }
        mIsLastActivityNull = mCurrentActivity == null;
        mCurrentActivity = act;
    }

    public static boolean isLastAcitivityNull() {
        return mIsLastActivityNull;
    }
}