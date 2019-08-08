package com.ben.yana;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

//display deleted notes
public class DeletedFragment extends BaseFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState)
    {
        //setup required data
        ((CustomApplication) getActivity().getApplication()).contentTitle = "Deleted";
        List<List<String>> mainContent = ((CustomApplication) getActivity().getApplication()).mainContent = dr.getData(((CustomApplication) getActivity().getApplication()).contentTitle, ((CustomApplication) getActivity().getApplication()).currentCategory);

        NoteListAdapter adapter = null;

        layout = setupList(listListener, adapter, R.layout.notelist, R.id.noteList, R.layout.noterow);
        layout = setupFab(R.mipmap.restoredeleted, unDeleteListener, layout, R.id.fabLoc2);
        layout = setupFab(R.mipmap.cleardeleted, deleteAllListener, layout, R.id.fabLoc1);

        super.onCreateView(inflater, vg, savedInstanceState);

        return layout;
    }


    //LISTENERS
    //removing all deleted notes button
    View.OnClickListener deleteAllListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            dr.clearDeleted();

            for(int i = 0; i <listAdapter.getCount(); i++)
            {
                listAdapter.remove(listAdapter.getItem(i));
            }
            ((CustomApplication)getActivity().getApplication()).mainContent = new ArrayList<>();


            listAdapter.refresh();
            getActivity().onBackPressed();
        }
    };

    //return selected notes tot he home list
    View.OnClickListener unDeleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);

                boolean success = dr.unDeleteNote(noteID);//update status in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter

            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//empty selected array


            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            ((CustomApplication)getActivity().getApplication()).SelectMode = false;
            listAdapter.refresh();

        }
    };
}

