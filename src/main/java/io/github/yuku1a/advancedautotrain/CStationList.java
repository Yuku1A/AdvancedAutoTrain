package io.github.yuku1a.advancedautotrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CStationInfoのその利用に最適化されたリストです。
 */
public class CStationList {
    private final List<CStationInfo> list;
    private int index = 0;

    /**
     * 新規のCStationListを初期化します
     */
    public CStationList() {
        this.list = new ArrayList<>();
    }

    /**
     * すでにあるCStationInfoのListからクラスを生成します
     * @param list すでに生成されたList
     * @throws NullPointerException 入力されたListがnullだった場合
     */
    public CStationList(List<CStationInfo> list) {
        this.list = new ArrayList<>(list);
    }

    /**
     * 現在のCStationInfoを取得します
     * @return 現在のCStationInfo、ない場合はnull
     */
    public CStationInfo get() {
        if (list.isEmpty())
            return null;
        return list.get(index);
    }

    /**
     * CStationInfoを追加します
     * @param info 追加するCStationInfo
     */
    public void add(CStationInfo info) {
        list.add(info);
    }

    /**
     * 次のCStationInfoに参照を切り替えます <br>
     * インデックスが範囲外になる場合は変わりません
     */
    public void forward() {
        if(index + 1 >= list.size())
            return;
        index++;
    }

    /**
     * 指定されたindexにあるCStationInfoを削除します。
     * @param index 削除するCStationInfoのindex
     * @throws IndexOutOfBoundsException インデックスが範囲外の場合にスローされます。
     */
    public void remove(int index) throws IndexOutOfBoundsException {
        list.remove(index);
    }

    /**
     * 内部のリストの変更不可能なビューを取得します。
     * @return 内部のリストの変更不可能なビュー
     */
    public List<CStationInfo> getList() {
        return Collections.unmodifiableList(list);
    }
}
