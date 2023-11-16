package io.github.yuku1a.advancedautotrain.schedaction;

/**
 * 予定された時刻に実行されるアクションを表現します。
 */
public class ScheduledAction implements Comparable<ScheduledAction> {
    private final long scheduletime;

    /**
     * @param scheduletime アクションの予定時刻
     */
    public ScheduledAction(long scheduletime) {
        this.scheduletime = scheduletime;
    }

    /**
     * アクションの予定時刻を取得します。
     * @return 予定時刻
     */
    public long getScheduletime() {
        return scheduletime;
    }

    @Override
    public int compareTo(ScheduledAction comparevalue) {
        var thisvalue = Long.valueOf(scheduletime);
        return thisvalue.compareTo(comparevalue.getScheduletime());
    }

    @Override
    public boolean equals(Object obj) {
        // 型チェックしてないと警告してくる
        if (!(obj instanceof ScheduledAction))
            return false;
        return compareTo((ScheduledAction) obj) == 0;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(scheduletime);
    }
}
