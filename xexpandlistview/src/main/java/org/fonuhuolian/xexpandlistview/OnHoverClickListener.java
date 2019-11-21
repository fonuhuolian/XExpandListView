package org.fonuhuolian.xexpandlistview;

public abstract class OnHoverClickListener {

    private int idsArr[];

    public OnHoverClickListener(int[] clickIds) {
        idsArr = clickIds;
    }

    public int[] getIdsArr() {
        return idsArr;
    }

    public abstract void onHoverItemClick(int id, int groupPositon);
}
