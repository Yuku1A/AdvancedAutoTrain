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
    private final String Announce;

    /**
     * @param name Stationを指定するための名前
     * @param signText Stationのsignのテキスト(2行目から4行目)
     * @param announce Stationに入るときに乗客に送信するメッセージ、ない場合はnullを使用できる
     */
    public CStationInfo(String name, String[] signText, String announce) {
        Name = name;
        if (signText != null) {
            if( signText.length == 3 )
                SignText = signText;
        }
        Announce = announce;
    }

    /**
     * Stationを指定するための名前
     * @return Stationを指定するための名前、なければnull
     */
    public String getName() {
        return Name;
    }

    /**
     * Stationのsignのテキスト(2行目から4行目)
     * @return Stationのsignのテキスト(2行目から4行目)、なければnull
     */
    public String[] getSignText() {
        return SignText;
    }

    /**
     * Stationに入るときに乗客に送信するメッセージ
     * @return Stationに入るときに乗客に送信するメッセージ、ない場合はnull
     */
    public String getAnnounce(){
        return Announce;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("Name", Name);
        map.put("SignText", new ArrayList<>(Arrays.asList(SignText)));
        map.put("Announce", Announce);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static CStationInfo deserialize(Map<String, Object> map) {
        var Name = (String) map.get("Name");
        var SignTextList = (ArrayList<String>) map.get("SignText");
        var SignText = SignTextList.toArray(new String[0]);
        var Announce = (String) map.get("Announce");
        return new CStationInfo(Name, SignText, Announce);
    }
}