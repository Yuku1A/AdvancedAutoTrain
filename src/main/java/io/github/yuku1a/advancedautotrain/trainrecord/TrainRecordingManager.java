package io.github.yuku1a.advancedautotrain.trainrecord;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * イベントを記録する列車の指定と<br>
 * 実際にワールドにある列車の紐づけを行うためのクラス
 */
public class TrainRecordingManager {
    private final IdentityHashMap<MinecartGroup, String> trainMap = new IdentityHashMap<>();
    private final List<String> recordingTrainNameList = new ArrayList<>();

    /**
     * イベントを記録する列車を指定します。
     * @param trainName イベントを記録する列車の名前
     */
    public void recordingRegister(String trainName) {
        // 重複する場合は追加しない
        if (recordingTrainNameList.contains(trainName))
            return;
        recordingTrainNameList.add(trainName);
    }

    /**
     * ワールドにある列車と名前の紐づけを解除します。
     * @param train ワールドにある列車
     */
    public void endRecording(MinecartGroup train) {
        trainMap.remove(train);
    }

    /**
     * 指定した列車のイベントの記録を終了します。
     * @param trainName イベントの記録を終了する列車の名前
     */
    public void recordingUnRegister(String trainName) {
        recordingTrainNameList.remove(trainName);
    }

    /**
     * 指定した列車と列車の名前の紐づけを行い、<br>
     * 他のイベントの記録に使用できるようにします。
     * @param train ワールドに実際にある列車
     * @param trainName 列車の名前
     * @return 実際に紐づけを行ったかどうか
     */
    public boolean startRecording(MinecartGroup train, String trainName) {
        // 事前にrecordingすることになっていなかったら登録しない
        if (!recordingTrainNameList.contains(trainName))
            return false;

        // 事前にrecordingすることになっていれば登録する
        trainMap.put(train, trainName);
        return true;
    }

    /**
     * ワールドに実際にある列車から列車の名前を取得します。
     * @param train ワールドに実際にある列車
     * @return 列車の名前。なければnull
     */
    public String getTrainName(MinecartGroup train) {
        return trainMap.get(train);
    }
}
