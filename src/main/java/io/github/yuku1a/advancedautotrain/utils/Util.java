package io.github.yuku1a.advancedautotrain.utils;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<String> searchInList(String query, List<String> list) {
        var resultList = new ArrayList<String>();
        for (var content : list) {
            if (content.startsWith(query))
                resultList.add(content);
        }
        return resultList;
    }

}
