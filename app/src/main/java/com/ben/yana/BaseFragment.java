package com.ben.yana;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.List;

//provides common functionality between multiple fragments
public class BaseFragment extends Fragment
{
    DataHandler dr = DataHandler.getInstance(getContext());

    //main content attributes
    ListView list;
    View layout;
    NoteListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState)
    {
        return  null;
        //let the fragments the inherit this handle it
    }

    //used to setup the main content on the list
    protected View setupList(AdapterView.OnItemClickListener listener, ListAdapter Adapter, int listLayout, int listView,int listRow)
    {
        List<List<String>> mainContent = ((CustomApplication)getActivity().getApplicationContext()).mainContent;

        listAdapter = (NoteListAdapter)Adapter;
        listAdapter = new NoteListAdapter(getContext(),listRow,mainContent);

        ((CustomApplication)getActivity().getApplication()).currentAdapter = listAdapter;

        View layout = inflate(listLayout);
        View mainLayout = inflate(listRow);

        //set listeners etc
        list = (ListView)layout.findViewById(listView);
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(listListener);
        list.setOnItemLongClickListener(longClickListener);

        return layout;
    }

    //set up a floating action button, can be used for any floating action button
    protected View setupFab(int resourceID, View.OnClickListener listener,View fabLayout, int fabID)
    {
        FloatingActionButton fab = (FloatingActionButton)fabLayout.findViewById(fabID);
        fab.setImageResource(resourceID);
        fab.setOnClickListener(listener);
        fab.show();
        return fabLayout;
    }

    //used to inflate a layout id
    protected View inflate(int layoutID)
    {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(layoutID,null,false);

        return layout;
    }

    //long click listener on the main content list
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l)
        {
            //get the main content
            ((CustomApplication)getActivity().getApplication()).SelectMode = true;
            List<List<String>> mainContent = ((CustomApplication)getActivity().getApplication()).mainContent;

            //check which was long clicked
            List<String> note = (mainContent.get(pos));

            //handling whether item is selected already etc.
            if(!((CustomApplication)getActivity().getApplication()).selected.contains(note))
            {
                ((CustomApplication)getActivity().getApplication()).selected.add(note);
                view.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
            }
            else
            {
                ((CustomApplication)getActivity().getApplication()).selected.remove(note);
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
            }

            //if you unselect the last one turn off select mode
            if(((CustomApplication)getActivity().getApplication()).selected.isEmpty())
            {
                ((CustomApplication)getActivity().getApplication()).SelectMode = false;
            }

            return false;
        }
    };

    //regular click listener on main content list
    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
        {
            //get content
            List<List<String>> mainContent = ((CustomApplication)getActivity().getApplication()).mainContent;
            List<String> notes = mainContent.get(pos);

            //check which was clicked
            String title = notes.get(0);
            String noteID = notes.get(2);

            if(((CustomApplication)getActivity().getApplication()).SelectMode)
            {
                //do nothing if select mode is active, let long click listener handle it
            }
            else
            {
                //launch note activity with requred info to display the note
                Intent i = new Intent(getActivity(),NoteActivity.class);
                i.putExtra("Title",title);
                i.putExtra("ID",noteID);
                startActivity(i);
            }
        }
    };
}