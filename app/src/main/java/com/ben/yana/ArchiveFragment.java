package com.ben.yana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

//displays and handles operations on notes that have been archived
public class ArchiveFragment extends BaseFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState)
    {
        ((CustomApplication)getActivity().getApplication()).contentTitle = "Archived";

        //update main content list to get the archived notes from the database
        List<List<String>> mainContent = ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

        NoteListAdapter adapter = null;

        //set up the required views for this fragment with the specialized listeners and correct icons etc.
        layout = setupList(listListener, adapter, R.layout.notelist,R.id.noteList,R.layout.noterow);
        layout = setupFab(R.mipmap.restorearchived,fabListener,layout,R.id.fabLoc1);
        layout = setupFab(R.mipmap.bin,deleteListener,layout,R.id.fabLoc2);
        super.onCreateView(inflater, vg, savedInstanceState);
        return layout;
    }

    //LISTENERS
    //Listener to remove selected notes from the archive
    View.OnClickListener fabListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //loop through selected notes
            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);//get note id

                boolean success = dr.unArchiveNote(noteID);//change status in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter
            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//empty selected notes


            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            //turn off select mode
            ((CustomApplication)getActivity().getApplication()).SelectMode = false;

            //refresh the adapter
            listAdapter.refresh();
        }
    };


    //listener to move all selected notes to the deleted section
    View.OnClickListener deleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //loop through selected notes
            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);//get note id

                boolean success = dr.deleteNote(noteID);//change status in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter
            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//remove from selected array


            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            //turn off select mode
            ((CustomApplication)getActivity().getApplication()).SelectMode = false;

            //refresh the adapter
            listAdapter.refresh();
        }
    };
}
