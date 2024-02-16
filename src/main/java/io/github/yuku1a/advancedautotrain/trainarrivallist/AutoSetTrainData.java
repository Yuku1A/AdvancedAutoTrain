package io.github.yuku1a.advancedautotrain.trainarrivallist;

import io.github.yuku1a.advancedautotrain.lspawn.ScheduledSpawn;

import java.util.List;

public record AutoSetTrainData (
    ScheduledSpawn lSpawn,
    List<AutoSetStationData> stationDataList,
    String trainName
){ }
