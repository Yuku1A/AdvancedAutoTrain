package io.github.yuku1a.advancedautotrain;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * CStationの動作に使われるデータです。
 */
public class CStationInfo implements ConfigurationSerializable {
    private final String Name;
    private String[] SignText;
    private final boolean EjectPassenger;
    private final boolean BlockPassenger;

    /**
     *
     * @param name Stationを指定するための名前
     * @param signText Stationのsignのテキスト(2行目から4行目)
     * @param ejectPassenger Stationに停車したときにejectするかどうか
     * @param blockPassenger Stationに停車したとき乗車不能にするかどうか
     */
    public CStationInfo(String name, String[] signText, boolean ejectPassenger, boolean blockPassenger) {
        Name = name;
        if( signText.length == 3 )
            SignText = signText;
        EjectPassenger = ejectPassenger;
        BlockPassenger = blockPassenger;
    }

    /**
     * Stationを指定するための名前
     * @return Stationを指定するための名前
     */
    public String getName() {
        return Name;
    }

    /**
     * Stationのsignのテキスト(2行目から4行目)
     * @return Stationのsignのテキスト(2行目から4行目)
     */
    public String[] getSignText() {
        return SignText;
    }

    /**
     * Stationに停車したときにejectするかどうか
     * @return Stationに停車したときにejectするかどうか
     */
    public boolean isEjectPassenger() {
        return EjectPassenger;
    }

    /**
     * Stationに停車したとき乗車不能にするかどうか
     * @return Stationに停車したとき乗車不能にするかどうか
     */
    public boolean isBlockPassenger() {
        return BlockPassenger;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("Name", Name);
        map.put("SignText", new ArrayList<>(Arrays.asList(SignText)));
        map.put("EjectPassenger", EjectPassenger);
        map.put("BlockPassenger", BlockPassenger);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static CStationInfo deserialize(Map<String, Object> map) {
        var Name = (String) map.get("Name");
        var SignTextList = (ArrayList<String>) map.get("SignText");
        var SignText = SignTextList.toArray(new String[0]);
        var EjectPassenger = (Boolean) map.get("EjectPassenger");
        var BlockPassenger = (Boolean) map.get("BlockPassenger");
        return new CStationInfo(Name, SignText, EjectPassenger, BlockPassenger);
    }
}