package org.fonuhuolian.xexpandlistview;

import android.content.Context;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseXExpandableListAdapter<T> extends BaseExpandableListAdapter implements XExpandListView.HoverAdapter {

    // 数据源
    private List<T> data = new ArrayList<>();

    private Context mContext;

    private ExpandableListView listView;

    public BaseXExpandableListAdapter(Context mContext, ExpandableListView listView) {
        this.mContext = mContext;
        this.listView = listView;
    }

    public BaseXExpandableListAdapter(Context mContext, ExpandableListView listView, List<T> data) {
        this.mContext = mContext;
        this.listView = listView;
        this.data.addAll(data);
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public int getHeaderState(int groupPosition, int childPosition) {
        final int childCount = getChildrenCount(groupPosition);
        if (childPosition == childCount - 1) {
            return PINNED_HEADER_PUSHED_UP;
        } else if (childPosition == -1
                && !listView.isGroupExpanded(groupPosition)) {
            return PINNED_HEADER_GONE;
        } else {
            return PINNED_HEADER_VISIBLE;
        }
    }

    public List<T> getData() {
        return data;
    }

    public Context getmContext() {
        return mContext;
    }

    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    public void addData(List<T> d) {
        data.addAll(d);
        notifyDataSetChanged();
    }
}
