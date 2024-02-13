package io.github.yuku1a.advancedautotrain.arrivallist;

import com.bergerkiller.bukkit.tc.utils.TimeDurationFormat;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WidgetsArrivalList {

    static void arrivalTrainView(CommandSender sender, List<ScheduledSign> arrivalTrainList) {
        // 時間表示のセットアップ
        var fmt = new TimeDurationFormat("HH:mm:ss");

        // UI
        sender.sendMessage("(time) (displayname) (description) (trainname)");

        // 情報の表示
        arrivalTrainList.forEach((v) -> sender.sendMessage(
            fmt.format(v.getScheduletime()) + " | " +
            v.getTrainName() + " | " +
            v.getTrainDescription() + " | " +
            v.getTrainInternalName()
        ));
    }
}
