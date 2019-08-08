package com.ben.yana;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ben.yana.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//adapts drawer content to expandable list
public class NavAdapter extends BaseExpandableListAdapter
{
    Context c;
    public List<String> parents;
    public HashMap<String,List<List<String>>> drawerItems;

    public NavAdapter(Context c, List<String> parents, Map<String,List<List<String>>> drawerItems)
    {
        this.parents = parents;
        this.drawerItems = (HashMap)drawerItems;
        this.c = c;
    }

    public HashMap<String,List<List<String>>> getData()
    {
        return drawerItems;
    }

    public void setData(HashMap<String,List<List<String>>> data)
    {
        this.drawerItems = data;
    }

    @Override
    //setup group row
    public View getGroupView(int pos, boolean expanded, View v, ViewGroup vg)
    {
        View row = v;

        if(row == null)
        {
            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.navrow, vg,false);
        }

        TextView t = (TextView)row.findViewById(R.id.text);

        t.setText(parents.get(pos));

        return row;
    }

    @Override
    //setup child row
    public View getChildView(int gPos, int cPos, boolean last, View v, ViewGroup vg)
    {
        View row;

        LayoutInflater inflater = getLayoutInflater();
        row = inflater.inflate(R.layout.navsubrow, vg, false);

        TextView t =(TextView)row.findViewById(R.id.text);

        t.setText((String)getChild(gPos,cPos));
        return row;
    }

    @Override
    public boolean isChildSelectable(int gPos, int cPos)
    {
        return true;
    }

    @Override
    public int getGroupCount()
    {
        return parents.size();
    }

    @Override
    public long getChildId(int gPos, int cPos)
    {
        return cPos;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public Object getChild(int gPos, int cPos)
    {
        return drawerItems.get(parents.get(gPos)).get(cPos).get(1);
    }

    @Override
    public long getGroupId(int gPos)
    {
        return gPos;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return drawerItems.get(parents.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int gPos) {
        return drawerItems.get(gPos);
    }

    //make it easier to get layout inflater
    public LayoutInflater getLayoutInflater()
    {
        return (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}