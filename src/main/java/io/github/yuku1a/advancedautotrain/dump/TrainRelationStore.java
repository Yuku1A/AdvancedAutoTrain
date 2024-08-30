package io.github.yuku1a.advancedautotrain.dump;

import io.github.yuku1a.advancedautotrain.Advancedautotrain;
import io.github.yuku1a.advancedautotrain.SaveDataStore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrainRelationStore extends SaveDataStore<Set<String>> {
    public TrainRelationStore(Advancedautotrain plugin) {
        super(plugin, "TrainRelation.yml");
    }

    @Override
    protected Object serialize(Set<String> data) {
        return List.copyOf(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<String> deserialize(Object obj) {
        var list = (List<String>) obj;
        return new HashSet<>(list);
    }
}
