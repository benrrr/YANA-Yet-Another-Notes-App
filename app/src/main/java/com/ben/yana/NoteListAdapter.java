package com.ben.yana;


import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

//used to display the main content in a list
public class NoteListAdapter extends ArrayAdapter<List<String>>
{
    Context c;
    DataHandler dr;

    public NoteListAdapter(Context c, int id, List<List<String>> data)
    {
        super(c,id,data);
        dr = new DataHandler(c);
        this.c = c;
    }

    @Override
    public View getView(int pos, View v, ViewGroup vg)
    {
        View row = v;

        //ensure content list is up to date
        List<List<String>> mainContent = ((CustomApplication)c.getApplicationContext()).mainContent = dr.getData(((CustomApplication) c.getApplicationContext()).contentTitle,((CustomApplication) c.getApplicationContext()).currentCategory);
        List<String> note = mainContent.get(pos);

        //inflate the row
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.noterow, vg, false);

        //set views
        TextView text = (TextView) row.findViewById(R.id.text);
        TextView title = (TextView) row.findViewById(R.id.title);
        FloatingActionButton img = (FloatingActionButton) row.findViewById(R.id.image);

        String noteTitle = note.get(0);
        String catID = note.get(1);
        String noteID = note.get(2);

        //use first letter of title to chose note image
        if(noteTitle.length()>0)
        {
            char icon = Character.toLowerCase(noteTitle.charAt(0));

            int iconID = getContext().getResources().getIdentifier(Character.toString(icon), "mipmap", getContext().getPackageName());
            img.setImageResource(iconID);
            img.setBackgroundColor(getContext().getResources().getColor(R.color.colorAccent,null));

            title.setText(noteTitle);
        }
        else
        {
            title.setText("Unknown");
            img.setImageResource(R.mipmap.ic_launcher_round);
        }

        //display category name
        String catName = dr.getCategoryName(catID);

        if(catName != "")
        {
            text.setText("Category: " + catName);
        }
        else
        {
            text.setText("Category: None");
        }


        //if the row is selected change color
        if(((CustomApplication)c.getApplicationContext()).selected.contains(note))
        {
            row.setBackgroundColor(c.getResources().getColor(R.color.colorAccent,null));
        }
        else
        {
            row.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary,null));
        }
        return row;
    }

    public void refresh()
    {
        this.notifyDataSetChanged();
    }
}
