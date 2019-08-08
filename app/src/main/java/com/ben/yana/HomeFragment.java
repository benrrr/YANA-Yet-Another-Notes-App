package com.ben.yana;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState)
    {
        //setup required data
        ((CustomApplication)getActivity().getApplication()).contentTitle = "Home";
        List<List<String>> mainContent = ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

        NoteListAdapter adapter = null;

        layout = setupList(listListener, adapter, R.layout.notelist, R.id.noteList, R.layout.noterow);
        layout = setupFab(R.mipmap.add_circle_black, fabListener, layout, R.id.fabLoc1);
        layout = setupFab(R.mipmap.archive, fabListener2, layout, R.id.fabLoc3);
        layout = setupFab(R.mipmap.bin,deleteListener,layout,R.id.fabLoc2);

        super.onCreateView(inflater, vg, savedInstanceState);
        return layout;
    }

    //LISTENERS
    //add new note or category listener
    View.OnClickListener fabListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            FrameLayout overlay = (FrameLayout) getActivity().findViewById(R.id.overlay);
            overlay.setVisibility(View.VISIBLE);

            Button newCat = (Button)getActivity().findViewById(R.id.newCat);
            newCat.setVisibility(View.VISIBLE);

            Button newNote = (Button)getActivity().findViewById(R.id.newNote);
            newNote.setText("New Note");

            EditText input = (EditText)getActivity().findViewById(R.id.newTitle);
            input.setHint("Note/Category Title");

            FrameLayout frame = (FrameLayout) getActivity().findViewById(R.id.frame);
            frame.setClickable(false);
        }

    };

    //archive selected notes
    View.OnClickListener fabListener2 = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);

                boolean success = dr.archiveNote(noteID);//update in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter
            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//remove from selected array

            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            ((CustomApplication)getActivity().getApplication()).SelectMode = false;

            listAdapter.refresh();
        }
    };


    //delete selected notes
    View.OnClickListener deleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Log.d("DELETE","START");
            Log.d("DELETE", "Selected Size" + ((CustomApplication)getActivity().getApplication()).selected.size());

            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);

                boolean success = dr.deleteNote(noteID);//update in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter

                Log.d("DELETE", " i = " + i + " Note ID = " + noteID + " Success = " + success);
            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//remove from selected array


            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            ((CustomApplication)getActivity().getApplication()).SelectMode = false;

            listAdapter.refresh();

        }
    };
}

