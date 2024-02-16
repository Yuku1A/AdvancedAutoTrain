package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.arrivallist.ScheduledSignSet;
import io.github.yuku1a.advancedautotrain.schedaction.OperationTimer;

import java.util.List;

public record AutoSetData (
    OperationTimer opTimer,
    List<AutoSetTrainData> trainDataList,
    List<ScheduledSignSet> stationlist,
    List<String> trainNameList
) { }
