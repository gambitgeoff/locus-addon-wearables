package com.asamm.locus.addon.wear.gui.trackrec.stats.view;

import android.view.ViewGroup;

import com.asamm.locus.addon.wear.common.communication.containers.trackrecording.TrackRecordingValue;
import com.asamm.locus.addon.wear.gui.trackrec.stats.model.TrackRecScreenConfigDto;

/*
 * Created by milan on 02.08.2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 * Copyright (C) 2018
 */
public interface TrackStatsScreenView {

    /** get ViewGroup for this screen  */
    ViewGroup getInflatedLayout();

    /** Refresh views of this screen after configuration changes */
    void refreshConfiguration(TrackRecScreenConfigDto screenConfig);

    /**  Store reference to the most recent TrackRecording value and refresh statistics */
    void handleNewTrackRecData(TrackRecordingValue data);

    void setAmbientMode(boolean isAmbient);

}
