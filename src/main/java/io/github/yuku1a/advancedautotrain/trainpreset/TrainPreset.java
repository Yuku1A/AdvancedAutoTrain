package io.github.yuku1a.advancedautotrain.trainpreset;

/**
 * 列車に適用される設定を表現します。
 */
public class TrainPreset {
    private final String trainName;
    private final String cstationListName;
    private final String routeName;
    private final String tag;

    public TrainPreset(String trainName, String cstationListName, String routeName, String tag) {
        this.trainName = trainName;
        this.cstationListName = cstationListName;
        this.routeName = routeName;
        this.tag = tag;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getCstationListName() {
        return cstationListName;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getTag() {
        return tag;
    }
}
