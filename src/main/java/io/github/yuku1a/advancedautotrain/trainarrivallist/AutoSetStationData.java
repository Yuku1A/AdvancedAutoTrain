package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import io.github.yuku1a.advancedautotrain.trainrecord.TrainRecordEntry;

public record AutoSetStationData (
    TrainRecordEntry recordEntry,
    ArrivalSignEntry arrivalSignEntry,
    ScheduledSignSet arrivalList,
    String stationName
) { }
