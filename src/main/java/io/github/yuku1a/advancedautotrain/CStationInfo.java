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
    private String[] SignText = null;
    private String[] SignText;

    /**
     * @param name Stationを指定するための名前
     * @param signText Stationのsignのテキスト(2行目から4行目)
     */
    public CStationInfo(String name, String[] signText) {
        Name = name;
        if (signText != null) {
            if( signText.length == 3 )
                SignText = signText;
        }
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

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("Name", Name);
        map.put("SignText", new ArrayList<>(Arrays.asList(SignText)));
        return map;
    }

    @SuppressWarnings("unchecked")
    public static CStationInfo deserialize(Map<String, Object> map) {
        var Name = (String) map.get("Name");
        var SignTextList = (ArrayList<String>) map.get("SignText");
        var SignText = SignTextList.toArray(new String[0]);
        return new CStationInfo(Name, SignText);
    }
}