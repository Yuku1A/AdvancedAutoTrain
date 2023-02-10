package io.github.yuku1a.advancedautotrain;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.commands.annotations.CommandTargetTrain;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.api.ITrainProperty;
import com.bergerkiller.bukkit.tc.properties.api.PropertyParser;
import com.bergerkiller.bukkit.tc.properties.api.context.PropertyParseContext;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * CStationListをプロパティとして実装するためのクラス
 */
public final class CStationListProperty implements ITrainProperty<CStationList> {
    private final CStationListStore store = CStationListStore.getInstance();
    private CStationListTemplateStore templateStore;

    @Override
    public CStationList get(TrainProperties properties) {
        return store.get(properties);
    }

    @Override
    public void set(TrainProperties properties, CStationList list) {
        store.put(properties, list);
    }

    @Override
    public CStationList getDefault() {
        return null;
    }

    @Override
    public Optional<CStationList> readFromConfig(ConfigurationNode config) {
        return Optional.empty();
    }

    @Override
    public void writeToConfig(ConfigurationNode config, Optional<CStationList> value) {

    }

    @PropertyParser("cstationlist|csl")
    public CStationList parser(PropertyParseContext<CStationList> ctx) {
        // ここにコマンドの入力がある
        var input = ctx.input();

        // コマンドの入力を元にテンプレートを取得してCStationListを作成
        return new CStationList(templateStore.get(input));
    }

    // なぜかわからないけど機能しないので放置
    @CommandTargetTrain
    @CommandMethod("train cstationlist <command>")
    @CommandDescription("Manage CStationList of Train")
    private void setProperty(
        final CommandSender sender,
        final TrainProperties prop,
        final @Argument("command") @Greedy String command
    ) {
        sender.sendMessage(command);
    }

    // なぜかわからないけど機能しないので放置
    @CommandMethod("train cstationlist")
    @CommandDescription("View CStationList of Train")
    private void getProperty(
        final CommandSender sender,
        final TrainProperties prop
        ) {
        sender.sendMessage("getProperty");
    }

    public void enable(Advancedautotrain plugin) {
        templateStore = plugin.getCStationListTemplateStore();
    }
}
