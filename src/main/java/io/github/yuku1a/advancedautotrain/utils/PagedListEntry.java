package io.github.yuku1a.advancedautotrain.utils;

/**
 * ページングされたリストのエントリを表します。
 * @param <T> 実際に格納されているデータ
 */
public class PagedListEntry<T> {

    /**
     * このデータの実際のインデックス
     * @return このデータの実際のインデックス
     */
    public int getIndex() { return index; }
    private final int index;

    /**
     * 実際に格納されているデータ
      * @return 実際に格納されているデータ
     */
    public T getData() { return data; }
    private final T data;

    /**
     * コンストラクタ
     * @param index 元のリストにおけるインデックス
     * @param data 実際のデータ
     */
    public PagedListEntry(int index, T data) {
        this.index = index;
        this.data = data;
    }
}
