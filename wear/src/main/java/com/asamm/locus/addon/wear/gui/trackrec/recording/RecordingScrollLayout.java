package com.asamm.locus.addon.wear.gui.trackrec.recording;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.asamm.locus.addon.wear.common.communication.containers.trackrecording.TrackRecordingValue;
import com.asamm.locus.addon.wear.gui.trackrec.TrackRecActivityState;

import java.util.List;

import locus.api.utils.Logger;

/**
 * Created by Milan Cejnar on 01.12.2017.
 * Asamm Software, s.r.o.
 */

public class RecordingScrollLayout extends ScrollView implements TrackRecordingControllerUpdatable {
	private static final String TAG = "RecordingScrollLayout";
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private List<TrackRecordingControllerUpdatable> mScreens = null;
	private GestureDetectorCompat mGestureDetector;
	/**
	 * Holds "index" of currently viewed screen/controller
	 */
	private int mActiveFeature = 0;

	public RecordingScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RecordingScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecordingScrollLayout(Context context) {
		super(context);
	}

	public void setFeatureItems(List<TrackRecordingControllerUpdatable> items) {
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		//Create a linear layout to hold each mScreens in the scroll view
		LinearLayout internalWrapper = new LinearLayout(getContext());
		internalWrapper.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		internalWrapper.setOrientation(LinearLayout.VERTICAL);
		addView(internalWrapper);
		this.mScreens = items;
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point screenSize = new Point();
		display.getSize(screenSize);
		for (TrackRecordingControllerUpdatable screen : items) {
			View v = screen.getControllersView();
			v.getLayoutParams().width = screenSize.x;
			v.getLayoutParams().height = screenSize.y;
			internalWrapper.addView(v);
		}
		setCustomScrolling();
	}

	private void setCustomScrolling() {
		mGestureDetector = new GestureDetectorCompat(getContext(), new MyGestureDetector());
		setOnTouchListener((v, event) -> {
			//If the user swipes
			if (mGestureDetector.onTouchEvent(event)) {
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				int featureHeight = v.getMeasuredHeight();
				int pageIdx = ((getScrollY() + (featureHeight / 2)) / featureHeight);
				scrollToPage(pageIdx, true);
				return true;
			} else {
				return false;
			}
		});
	}

	public void scrollToNextPage() {
		int pIdx = mActiveFeature;
		boolean overflow = pIdx == mScreens.size() - 1;
		if (overflow) {
			scrollToPage(0, false);
		} else {
			scrollToPage(pIdx + 1, true);
		}
	}

	public void scrollToPreviousPage() {
		int pIdx = mActiveFeature;
		boolean overflow = pIdx == 0;
		if (overflow) {
			scrollToPage(mScreens.size() - 1, false);
		} else {
			scrollToPage(pIdx - 1, true);
		}
	}

	private void scrollToPage(int featuredPagedIdx, boolean smoothScroll) {
		int h = this.getMeasuredHeight();
		mActiveFeature = featuredPagedIdx;
		int scrollTo = mActiveFeature * h;
		if (smoothScroll) {
			smoothScrollTo(0, scrollTo);
		} else {
			scrollTo(0, scrollTo);
		}
	}

	@Override
	public void onTrackActivityStateChange(Activity context, TrackRecActivityState newState) {
		for (TrackRecordingControllerUpdatable scr : mScreens) {
			scr.onTrackActivityStateChange(context, newState);
		}
	}

	@Override
	public void onNewTrackRecordingData(Activity context, TrackRecordingValue newData) {
		for (TrackRecordingControllerUpdatable scr : mScreens) {
			scr.onNewTrackRecordingData(context, newData);
		}
	}

	@Override
	public ViewGroup getControllersView() {
		return this;
	}

	public void setAmbient(boolean isAmbient) {
		for (TrackRecordingControllerUpdatable screen : mScreens) {
			screen.setAmbient(isAmbient);
		}
	}

	@Override
	public int getControllerScreenIdx() {
		// This is only container/delegate wrapping multiple screens having no usable screenId on its own
		return -1;
	}

	private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				//right to left
				if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					int pIdx = mActiveFeature;
					pIdx = (pIdx < (mScreens.size() - 1)) ? pIdx + 1 : mScreens.size() - 1;
					scrollToPage(pIdx, true);
					return true;
				}
				//left to right
				else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
					int pIdx = mActiveFeature;
					pIdx = (pIdx > 0) ? pIdx - 1 : 0;
					scrollToPage(pIdx, true);
					return true;
				}
			} catch (Exception e) {
				Logger.logE(TAG, "There was an error processing the Fling event.", e);
			}
			return false;
		}
	}
}
