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

//displays notes from specific category in main content list
public class CategoryFragment extends BaseFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceState)
    {
        //setup necessary views and content listed to display the category notes
        NoteListAdapter adapter = null;

        ((CustomApplication)getActivity().getApplication()).contentTitle = "Categories";
        List<List<String>> mainContent = ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

        layout = setupList(listListener, adapter, R.layout.notelist, R.id.noteList, R.layout.noterow);
        layout = setupFab(R.mipmap.add_circle, fabListener, layout, R.id.fabLoc1);
        layout = setupFab(R.mipmap.bin,deleteListener,layout,R.id.fabLoc2);

        super.onCreateView(inflater, vg, savedInstanceState);

        return layout;
    }


    //LISTENERS
    //create a note in this category button
    View.OnClickListener fabListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //set required visibilty and clickables for the overlay to work
            FrameLayout overlay = (FrameLayout) getActivity().findViewById(R.id.overlay);
            overlay.setVisibility(View.VISIBLE);

            Button newCat = (Button)getActivity().findViewById(R.id.newCat);
            newCat.setVisibility(View.GONE);

            Button newNote = (Button)getActivity().findViewById(R.id.newNote);
            newNote.setText("Create Note");

            EditText input = (EditText)getActivity().findViewById(R.id.newTitle);
            input.setHint("Note Title");

            FrameLayout frame = (FrameLayout) getActivity().findViewById(R.id.frame);
            frame.setClickable(false);
        }
    };

    //delete selected button listener
    View.OnClickListener deleteListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            //loop through selected
            for(int i = 0; i < ((CustomApplication)getActivity().getApplication()).selected.size(); i++)
            {
                List<String> note = ((CustomApplication)getActivity().getApplication()).selected.get(i);
                String noteID = note.get(2);

                boolean success = dr.deleteNote(noteID);//update status in database
                listAdapter.remove(listAdapter.getItem(listAdapter.getPosition(note)));//remove from list adapter
            }

            ((CustomApplication)getActivity().getApplication()).selected = new ArrayList<List<String>>();//empty from selected array

            //update the global list
            ((CustomApplication)getActivity().getApplication()).mainContent = dr.getData(((CustomApplication)getActivity().getApplication()).contentTitle,((CustomApplication)getActivity().getApplication()).currentCategory);

            ((CustomApplication)getActivity().getApplication()).SelectMode = false;

            listAdapter.refresh();

        }
    };
}
