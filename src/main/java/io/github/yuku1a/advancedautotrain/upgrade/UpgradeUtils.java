package io.github.yuku1a.advancedautotrain.upgrade;

public class UpgradeUtils {
    /**
     * YAMLから変換されたオブジェクトをデータに変換します
     * @param obj YAMLからデシリアライズされたときの(String, Object)のObject
     * @return データ
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }
}
