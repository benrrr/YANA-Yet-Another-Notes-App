package com.ben.yana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.location.Geocoder;
import android.location.Address;

//handles adapting note components for the list
public class CompListAdapter extends BaseAdapter
{
    Map<String,Map<String,String>> data;
    List<String> headers;
    DataHandler dr;

    LayoutInflater inflater;
    Context context;

    String noteID;

    public CompListAdapter(Map<String,Map<String,String>> data, Context context, String noteID)
    {
        //setup required data
        dr = new DataHandler(context);

        this.data = data = ((CustomApplication)context.getApplicationContext()).currentComponents = dr.getNoteComponents(noteID);
        headers = new ArrayList<>(((CustomApplication)context.getApplicationContext()).currentComponents.keySet());
        notifyDataSetChanged();

        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.noteID = noteID;
    }

    @Override
    public int getCount()
    {
        //how many items in list
        return ((CustomApplication)context.getApplicationContext()).currentComponents.size();
    }

    @Override
    public View getView(int pos, View v, ViewGroup vg)
    {
        View row = v;

        //get components
        data = ((CustomApplication)context.getApplicationContext()).currentComponents = dr.getNoteComponents(noteID);
        headers = new ArrayList<>(((CustomApplication)context.getApplicationContext()).currentComponents.keySet());
        notifyDataSetChanged();

        //check which layout should be used for this component
        int type = getItemViewType(pos);

        switch(type)
        {
            case -1:
            {
                Log.d("LAYOUT", "ERROR");
            }
            case 0:
            {
                //if text component inflate the text row
                row = inflater.inflate(R.layout.text_row, null, false);
                TextView text = (TextView)row.findViewById(R.id.text);

                String CompID = getItem(pos).get("ID");
                String CompType = getItem(pos).get("type");
                String CompPath = getItem(pos).get("path");

                String textContent = "";

                //use the stored path to load the text from file
                try
                {
                    FileInputStream inputStream;
                    inputStream = context.openFileInput(CompPath);
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader buff = new BufferedReader(reader);
                    String recvline;

                    while((recvline = buff.readLine())!= null)
                    {
                        textContent += recvline;
                    }

                    reader.close();
                }
                catch(Exception e)
                {
                    Log.d("FILE",e.toString());
                }

                //set loaded text
                text.setText(textContent);
                break;
            }
            case 1:
            {
                //if image component inflate the image row
                row = inflater.inflate(R.layout.image_row, null, false);
                ImageView imgView = (ImageView)row.findViewById(R.id.image);
                String CompID = getItem(pos).get("ID");
                String CompType = getItem(pos).get("type");
                String CompPath = getItem(pos).get("path");

                //use stored path to load the image as a bitmap and set it to the image view
                try
                {
                    File noteDir = new File(context.getFilesDir(),noteID);
                    File image = new File(noteDir,CompPath);

                    Bitmap bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
                    imgView.setImageBitmap(bmp);
                }
                catch(Exception e)
                {
                    Log.d("IMAGE", e.toString());
                }

                break;
            }
            case 2:
            {
                //if map component inflate the map row
                row = inflater.inflate(R.layout.map_row, null, false);
                TextView text = (TextView)row.findViewById(R.id.loc);

                String CompID = getItem(pos).get("ID");
                String CompType = getItem(pos).get("type");
                String CompPath = getItem(pos).get("path");

                String details = CompID + " " + CompType + " " + CompPath;

                //if there was a problem with location the path will contain not found in it
                if(!CompPath.contains("not found"))
                {
                    //map path is formatted as longitude#latitude when stored to make it easy to split
                    String[] parts = CompPath.split("#");
                    String longitudeStr = parts[0];
                    String latitudeStr = parts[1];

                    Double longitude = Double.parseDouble(longitudeStr);
                    Double latitude = Double.parseDouble(latitudeStr);

                    //use geocoder to get address from the long/lat
                    Geocoder geo = new Geocoder(context, Locale.getDefault());
                    try
                    {
                        List<Address> addresses = geo.getFromLocation(longitude,latitude,1);
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();

                        //depending on place some may be null, only append if not null
                        if(city != null)
                        {
                            address = address + " " + city;
                        }

                        if(state != null)
                        {
                            address = address + " " + state;
                        }

                        if(country != null)
                        {
                            address = address + " " + country;
                        }

                        if(postalCode != null)
                        {
                            address = address + " " + postalCode;
                        }

                        text.setText(address);
                    }
                    catch (IOException e)
                    {
                        Log.d("LOCATION",e.toString());
                        //if error getting address set text error
                        text.setText("error");
                    }
                }
                else
                {
                    text.setText(CompPath);
                }
                break;
            }
        }

        return row;
    }

    @Override
    public Map<String,String> getItem(int pos)
    {
        return data.get(headers.get(pos));
    }

    @Override
    public long getItemId(int pos)
    {
        return pos;
    }

    @Override
    public int getViewTypeCount()
    {
        return 3;//3 different layouts: text, image, map
    }

    @Override
    public int getItemViewType(int pos)
    {
        //get updated components list
        data = ((CustomApplication)context.getApplicationContext()).currentComponents = dr.getNoteComponents(noteID);
        headers = new ArrayList<>(((CustomApplication)context.getApplicationContext()).currentComponents.keySet());
        notifyDataSetChanged();

        //check what this current component has stored as type
        if(data.get(headers.get(pos)).get("type").equals("text"))
        {
            return 0;
        }
        else if(data.get(headers.get(pos)).get("type").equals("image"))
        {
            return 1;
        }
        else if(data.get(headers.get(pos)).get("type").equals("map"))
        {
            return 2;
        }
        else
        {
            return -1;
        }
    }
}
