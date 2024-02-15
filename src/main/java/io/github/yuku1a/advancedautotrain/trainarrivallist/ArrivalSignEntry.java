package io.github.yuku1a.advancedautotrain.trainarrivallist;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * 駅の看板に表示する列車の情報を列車に紐づけるためのデータ
 */
public class ArrivalSignEntry implements ConfigurationSerializable {

    /**
     * コンストラクタ。
     * secondsOffsetはデフォルトでは-2になります。
     * @param CStationName この列車の情報を表示するArrivalListに連動するCStationの名前
     * @param displayName 列車の表示名
     */
    public ArrivalSignEntry(String CStationName, String displayName) {
        this(CStationName, displayName, -2, null);
    }

    /**
     * コンストラクタ。
     * secondsOffsetはデフォルトでは-2になります。
     * @param CStationName この列車の情報を表示するArrivalListに連動するCStationの名前
     * @param displayName 列車の表示名
     * @param trainDescription 列車の説明
     */
    public ArrivalSignEntry(String CStationName, String displayName, String trainDescription) {
        this(CStationName, displayName, -2, trainDescription);
    }

    /**
     * コンストラクタ。
     * @param CStationName この列車の情報を表示するArrivalListに連動するCStationの名前
     * @param displayName 列車の表示名
     * @param secondsOffset 実際の時刻に対するオフセット、秒単位
     */
    public ArrivalSignEntry(String CStationName, String displayName, long secondsOffset) {
        this(CStationName, displayName, secondsOffset, null);
    }

    /**
     * @param CStationName この列車の情報を表示するArrivalListに連動するCStationの名前
     * @param displayName 列車の名前
     * @param trainDescription 列車の説明
     * @param secondsOffset 実際の時刻に対するオフセット、秒単位
     */
    public ArrivalSignEntry(String CStationName, String displayName, long secondsOffset, String trainDescription) {
        this.CStationName = CStationName;
        this.displayName = displayName;
        this.secondsOffset = secondsOffset;
        this.trainDescription = trainDescription;
    }

    /**
     * この列車の情報を表示するArrivalListに連動するCStationの名前
     * @return この列車の情報を表示するArrivalListに連動するCStationの名前
     */
    public String getCStationName() { return CStationName; }
    private final String CStationName;

    /**
     * 列車が実際に動く時刻に対するオフセット値、秒単位
     * @return 列車が実際に動く時刻に対するオフセット値、秒単位
     */
    public long getSecondsOffset() { return secondsOffset; }
    private final long secondsOffset;

    /**
     * 表示される列車の名前
     * @return 表示される列車の名前
     */
    public String getTrainName() { return displayName; }
    private final String displayName;

    /**
     * 列車の詳細な説明
     * @return 列車の詳細な説明、ない場合null
     */
    public String getTrainDescription() { return trainDescription; }
    private final String trainDescription;

    public static ArrivalSignEntry deserialize(Map<String, Object> map) {
        var cstationname = (String) map.get("cstationname");
        var name = (String) map.get("name");
        var desc = (String) map.get("desc");
        var offset = Long.parseLong((String) map.get("offset"));
        return new ArrivalSignEntry(cstationname, name, offset, desc);
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("cstationname", CStationName);
        map.put("name", displayName);
        map.put("desc", trainDescription);
        map.put("offset", String.valueOf(secondsOffset));
        return map;
    }
}
