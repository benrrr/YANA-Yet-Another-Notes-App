package com.ben.yana;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.widget.ArrayAdapter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

//handles all things about displaying note components and interacting with them
public class NoteActivity extends AppCompatActivity implements LocationListener
{
    DataHandler dr;
    List<String> componentsHeader = new ArrayList<String>();
    List<List<String>> categories;
    String noteID;
    String titleText;
    FrameLayout overlay;
    LayoutInflater inflater;
    View newComp;
    View editText;
    View editImage;
    View editMap;
    View setCat;
    FloatingActionButton fab;
    CompListAdapter adapter;
    String chosenImg;
    String componentEditingPath;
    String componentEditingID;
    Location loc;

    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_view);
        dr = DataHandler.getInstance(getApplicationContext());

        try
        {
            //used as a hint when making a new note
            FileOutputStream os = openFileOutput("hint", MODE_PRIVATE);
            os.write("Hint: \nClick the plus button below to add note components\n or the tag button to set a category".getBytes());
            os.close();

            ((CustomApplication)getApplication()).currentComponents = dr.getNoteComponents(noteID);
            adapter.notifyDataSetChanged();
        }
        catch(Exception e)
        {
            Log.d("EDITING",e.toString());
        }

        Bundle extras = getIntent().getExtras();

        //if the extras are not coming from the create new note option
        //that means there will be an id in the extras, so we get the already
        //existing note components
        if((titleText = extras.getString("newTitle"))== null)//not a new note
        {
            //get info
            titleText = extras.getString("Title");
            noteID = extras.getString("ID");

            //get components
            ((CustomApplication)getApplication()).currentComponents = dr.getNoteComponents(noteID);
            componentsHeader = new ArrayList<>(((CustomApplication)getApplication()).currentComponents.keySet());
        }
        else//new note
        {
            //insert new note and hint component
            noteID = Long.toString(dr.insertNewNote(titleText,((CustomApplication)getApplication()).currentCategory));
            dr.insertComponent(noteID,"text","hint");
            List<List<String>> mainContent = ((CustomApplication)getApplicationContext()).mainContent = dr.getData(((CustomApplication)getApplicationContext()).contentTitle,((CustomApplication)getApplicationContext()).currentCategory);

            List<String> note = mainContent.get(mainContent.size()-1);
            ((NoteListAdapter)((CustomApplication)this.getApplication()).currentAdapter).add(note);

            ((NoteListAdapter)((CustomApplication)this.getApplication()).currentAdapter).refresh();
        }

        //setup the many views used for interacting with note components
        overlay = (FrameLayout)findViewById(R.id.overlay);
        fab = (FloatingActionButton)findViewById(R.id.newComp);
        fab.setOnClickListener(fabListener);

        Toolbar toolbar = findViewById(R.id.noteToolbar);
        toolbar.setTitle(titleText);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //pre inflate the editing layouts
        inflater = getLayoutInflater();
        newComp = inflater.inflate(R.layout.new_comp,null,false);
        editText = inflater.inflate(R.layout.edit_text_comp,null,false);
        editImage = inflater.inflate(R.layout.edit_image_comp, null, false);
        editMap = inflater.inflate(R.layout.edit_map_comp, null, false);
        setCat = inflater.inflate(R.layout.set_cat, null, false);

        //set the button listeners ont he different layours
        Button newText = (Button)newComp.findViewById(R.id.newText);
        Button newImage = (Button)newComp.findViewById(R.id.newImage);
        Button newMap = (Button)newComp.findViewById(R.id.newMap);
        Button saveText = (Button)editText.findViewById(R.id.save);
        Button saveImage = (Button)editImage.findViewById(R.id.save);
        Button saveMap = (Button)editMap.findViewById(R.id.save);
        Button imgGallery = (Button)editImage.findViewById(R.id.Gallery);
        Button imgCamera = (Button)editImage.findViewById(R.id.Camera);
        Button imgURL = (Button)editImage.findViewById(R.id.URL);
        FloatingActionButton setCatfab = (FloatingActionButton)findViewById(R.id.setCat);
        setCatfab.setImageResource(R.mipmap.cat);
        setCatfab.setOnClickListener(setCatListener);

        newText.setOnClickListener(textListener);
        newImage.setOnClickListener(imageListener);
        newMap.setOnClickListener(mapListener);
        saveText.setOnClickListener(saveTextListener);
        saveImage.setOnClickListener(saveImageListener);
        saveMap.setOnClickListener(saveMapListener);
        imgGallery.setOnClickListener(imgGalleryListener);
        imgCamera.setOnClickListener(imgCameraListener);
        imgURL.setOnClickListener(imgURLListener);

        //setup the list of components
        ListView list = (ListView)findViewById(R.id.compList);
        adapter = new CompListAdapter(((CustomApplication)getApplication()).currentComponents,getApplicationContext(),noteID);
        list.setAdapter(adapter);
        list.setOnItemClickListener(listListener);

        //setup category list for choosing category
        ListView catList = (ListView)setCat.findViewById(R.id.catList);
        categories = dr.getCategories();
        List<String> IDs = new ArrayList<>();

        for(int i = 0; i < categories.size(); i++)
        {
            IDs.add(categories.get(i).get(1));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,IDs);
        catList.setAdapter(adapter);
        catList.setOnItemClickListener(catListener);

        //ensure we have correct permissions
        if (ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(NoteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        else
        {
            //request location updates for adding map components
            LocationManager locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locMan.requestLocationUpdates(locMan.GPS_PROVIDER,10,1,NoteActivity.this);
        }
    }

    //updating category listener
    ListView.OnItemClickListener catListener = new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
        {
            List<String> cat = categories.get(pos);
            String catID = cat.get(0);

            dr.updateCat(catID, noteID);

            onBackPressed();
        }
    };

    //enabling the back button in the toolbar to uses the general back press
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //component clicked listener
    AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
        {
            //get components
            Map<String,Map<String,String>> currentComponents = ((CustomApplication)getApplication()).currentComponents;
            List<String> headers = new ArrayList<>(currentComponents.keySet());
            Map<String,String> comp = currentComponents.get(headers.get(pos));

            String type = comp.get("type");

            //prepare overlay
            overlay.removeAllViews();
            overlay.setVisibility(View.VISIBLE);

            componentEditingPath = comp.get("path");
            componentEditingID = comp.get("ID");

            //check type of component
            if(type.equals("text"))
            {
                //if text add the edit text overlay
                overlay.addView(editText);
                Button delete = (Button)editText.findViewById(R.id.delete);
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(deleteComponentListener);

                EditText editor = (EditText)editText.findViewById(R.id.input);
                TextView current = (TextView)view.findViewById(R.id.text);
                String cur = current.getText().toString();
                editor.setText(cur);
            }
            else if(type.equals("image"))
            {
                //if image add the edit image overlay
                Button save = (Button)editImage.findViewById(R.id.save);
                Button delete = (Button)editImage.findViewById(R.id.delete);
                Button gallery = (Button)editImage.findViewById(R.id.Gallery);
                Button camera = (Button)editImage.findViewById(R.id.Camera);
                Button url = (Button)editImage.findViewById(R.id.URL);
                EditText urlIn = (EditText)editImage.findViewById(R.id.url);
                TextView text = (TextView)editImage.findViewById(R.id.text);

                gallery.setVisibility(View.GONE);
                camera.setVisibility(View.GONE);
                url.setVisibility(View.GONE);
                save.setVisibility(View.GONE);
                urlIn.setVisibility(View.GONE);
                text.setVisibility(View.GONE);

                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(deleteComponentListener);

                overlay.addView(editImage);
                ImageView image = (ImageView)editImage.findViewById(R.id.preview);

                //show the image
                File noteDir = new File(getFilesDir(),noteID);

                File file = new File(noteDir,componentEditingPath);

                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                image.setImageBitmap(bmp);

            }
            else if(type.equals("map"))
            {
                //if map show the edit map overlay
                Button delete = (Button)editMap.findViewById(R.id.delete);
                Button save = (Button)editMap.findViewById(R.id.save);
                Button map = (Button)editMap.findViewById(R.id.map);

                overlay.addView(editMap);
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(deleteComponentListener);
                map.setVisibility(View.VISIBLE);
                map.setOnClickListener(viewOnMapListener);
                save.setVisibility(View.GONE);
            }
        }
    };

    //viewing saved location on map
    View.OnClickListener viewOnMapListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Map<String,Map<String,String>> currentComponents =((CustomApplication)getApplication()).currentComponents;

            List<String> headers = new ArrayList<>(currentComponents.keySet());

            for(int i = 0; i < currentComponents.size(); i++)
            {
                Map<String,String> comp = currentComponents.get(headers.get(i));

                if(comp.get("ID").equals(componentEditingID))
                {
                    String CompPath = comp.get("path");

                    if(!CompPath.contains("not found"))
                    {
                        //parse stored coords
                        String[] parts = CompPath.split("#");
                        String longitudeStr = parts[0];
                        String latitudeStr = parts[1];

                        Double longitude = Double.parseDouble(longitudeStr);
                        Double latitude = Double.parseDouble(latitudeStr);

                        //setup intent to go to google maps with required data
                        String link = "http://maps.google.com/maps?q=loc:" + longitude + "," + latitude;
                        Intent in = new Intent(Intent.ACTION_VIEW,Uri.parse(link));
                        startActivity(in);
                    }
                    break;
                }
            }
        }
    };

    //delete a components listener, for all component types
    View.OnClickListener deleteComponentListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            dr.deleteComponent(componentEditingID);
            Map<String,Map<String,String>> currentComponents =((CustomApplication)getApplication()).currentComponents;

            List<String> headers = new ArrayList<>(currentComponents.keySet());

            for(int i = 0; i < currentComponents.size(); i++)
            {
                Map<String,String> comp = currentComponents.get(headers.get(i));

                if(comp.get("ID").equals(componentEditingID))
                {
                    adapter.data.remove(comp);
                    break;
                }
            }
            ((CustomApplication)getApplication()).currentComponents = dr.getNoteComponents(noteID);

            adapter.notifyDataSetChanged();

            onBackPressed();
        }
    };

    //new components listener
    View.OnClickListener fabListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            overlay.removeAllViews();
            overlay.setVisibility(View.VISIBLE);
            overlay.addView(newComp);
            ImageView img = (ImageView)editImage.findViewById(R.id.preview);
            img.setImageResource(R.mipmap.ic_launcher_round);
            EditText input = (EditText)editText.findViewById(R.id.input);
            input.setText("");
            fab.setClickable(false);
        }
    };

    //set note category listener
    View.OnClickListener setCatListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            overlay.removeAllViews();
            overlay.setVisibility(View.VISIBLE);
            overlay.addView(setCat);
            fab.setClickable(false);
        }
    };

    //text note listener
    View.OnClickListener textListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            overlay.removeAllViews();
            overlay.addView(editText);
        }
    };

    //image note listener
    View.OnClickListener imageListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            overlay.removeAllViews();
            overlay.addView(editImage);
            Button save = (Button)editImage.findViewById(R.id.save);
            Button delete = (Button)editImage.findViewById(R.id.delete);
            Button gallery = (Button)editImage.findViewById(R.id.Gallery);
            Button camera = (Button)editImage.findViewById(R.id.Camera);
            Button url = (Button)editImage.findViewById(R.id.URL);
            EditText urlIn = (EditText)editImage.findViewById(R.id.url);
            TextView text = (TextView)editImage.findViewById(R.id.text);

            gallery.setVisibility(View.VISIBLE);
            camera.setVisibility(View.VISIBLE);
            url.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            urlIn.setVisibility(View.VISIBLE);
            text.setVisibility(View.VISIBLE);

            delete.setVisibility(View.GONE);
        }
    };

    //map note listener
    View.OnClickListener mapListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            overlay.removeAllViews();
            overlay.addView(editMap);
            Button delete = (Button)editMap.findViewById(R.id.delete);
            Button save = (Button)editMap.findViewById(R.id.save);
            Button map = (Button)editMap.findViewById(R.id.map);

            map.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            save.setVisibility(View.VISIBLE);
        }
    };

    //saving text
    View.OnClickListener saveTextListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            EditText input = (EditText)editText.findViewById(R.id.input);
            String text = input.getText().toString();

            //if not null it means its editing an already made file
            if(componentEditingPath != null)
            {
                try
                {
                    //write over file
                    FileOutputStream os = openFileOutput(componentEditingPath, MODE_PRIVATE);
                    os.write(text.getBytes());
                    os.close();

                    ((CustomApplication)getApplication()).currentComponents = dr.getNoteComponents(noteID);
                    adapter.notifyDataSetChanged();
                }
                catch(Exception e)
                {
                    Log.d("EDITING",e.toString());
                }
                componentEditingPath = null;
                componentEditingID = null;
            }
            else//otherwise generate a new file
            {
                try
                {
                    //generate random file name, write to it
                    String filename = UUID.randomUUID().toString();
                    FileOutputStream os;
                    os = openFileOutput(filename, Context.MODE_PRIVATE);
                    os.write(text.getBytes());
                    os.close();

                    //insert in to database
                    dr.insertComponent(noteID,"text",filename);
                    ((CustomApplication)getApplication()).currentComponents = dr.getNoteComponents(noteID);
                    adapter.notifyDataSetChanged();

                    for(Map.Entry<String,Map<String,String>> comp : ((CustomApplication)getApplication()).currentComponents.entrySet())
                    {
                        if(comp.getValue().get("ID") == noteID)
                        {
                            adapter.data.put(comp.getKey(),comp.getValue());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                catch(Exception e)
                {
                    Log.d("FILE",e.toString());
                }

            }
            onBackPressed();
        }
    };

    //save image
    View.OnClickListener saveImageListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            EditText input = (EditText)editText.findViewById(R.id.input);
            String text = input.getText().toString();
            //save the path that was set when the image was chosen

            while(chosenImg == null);
            dr.insertComponent(noteID,"image",(chosenImg.toString().replace("-","")));

            onBackPressed();
        }
    };

    //save map
    View.OnClickListener saveMapListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if(loc == null)
            {
                //if null, location hasn't been found
                dr.insertComponent(noteID,"map","Location not found");
            }
            else
            {
                //otherwise format the location and store it
                String location = Double.toString(loc.getLongitude()) + "#" + Double.toString(loc.getLatitude());
                dr.insertComponent(noteID,"map",location);
            }

            onBackPressed();
        }
    };

    //set the location
    @Override
    public void onLocationChanged(Location loc)
    {
        this.loc = loc;
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //do nothing
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        //do nothing
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        //do nothing
    }

    //choosing image from the gallery
    View.OnClickListener imgGalleryListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, GALLERY_REQUEST);
        }
    };

    //taking image from the camera
    View.OnClickListener imgCameraListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Intent i = new Intent();
            i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i,CAMERA_REQUEST);
        }
    };

    //taking image from a url
    View.OnClickListener imgURLListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            EditText urlInput = (EditText)findViewById(R.id.url);
            String urlText = urlInput.getText().toString();

            try
            {
                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo     = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected())
                {
                    new DownloadImage().execute(urlText);
                }
                else
                {
                    urlInput.setHint("No network connection available.");
                }
            }
            catch(Exception e)
            {
                Log.d("IMAGE", e.toString());
            }
        }
    };

    //for downloading the image
    private class DownloadImage extends AsyncTask<String,Void,String>
    {
        Bitmap image = null;
        protected String doInBackground(String ... urls)
        {
            try
            {
                URL url = new URL(urls[0]);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch(Exception e)
            {
                Log.d("NETWORK",e.toString());
            }

            if(image == null)
            {
                return "FAIL";
            }
            else
            {
                return "SUCCESS";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            EditText urlInput = (EditText)findViewById(R.id.url);

            if(result == "SUCCESS")
            {
                ImageView imgPre = (ImageView)editImage.findViewById(R.id.preview);
                imgPre.setImageBitmap(image);
                chosenImg = saveBitmap(image);
                urlInput.setText("");
            }
            else
            {
                urlInput.setText("");
                urlInput.setHint("Error: Possibly Invalid URL");
            }

        }
    }

    //handling the result of intents for getting content
    @Override
    protected void onActivityResult(int req, int res, Intent i)
    {
        //image from gallery request
        if (req == GALLERY_REQUEST && res == RESULT_OK)
        {
            try
            {
                //save the image
                Uri imgUri = i.getData();
                Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                ImageView imgPre = (ImageView) editImage.findViewById(R.id.preview);
                imgPre.setImageBitmap(img);

                chosenImg = saveBitmap(img);
            }
            catch (Exception e)
            {
                Log.d("IMAGE", e.toString());
            }

        }
        //image from camera
        else if (req == CAMERA_REQUEST && res == RESULT_OK)
        {
            try
            {
                Bitmap img = (Bitmap) i.getExtras().get("data");
                ImageView imgPre = (ImageView) editImage.findViewById(R.id.preview);
                imgPre.setImageBitmap(img);

                chosenImg = saveBitmap(img);

            }
            catch (Exception e)
            {
                Log.d("IMAGE", e.toString());
            }
        }
    }

    //copy image so we maintain permission to use it in the future
    public String saveBitmap(Bitmap img)
    {
        UUID imageName = UUID.randomUUID();
        String id = imageName.toString();
        id = id.replace("-","");

        File noteDir = new File(getFilesDir(), noteID);

        if(!noteDir.exists())
        {
            noteDir.mkdir();
        }

        try
        {
            File image = new File(noteDir,id);

            FileOutputStream os = new FileOutputStream(image);
            img.compress(Bitmap.CompressFormat.PNG,100,os);
            os.flush();
            os.close();
            return id;
        }
        catch(Exception e)
        {
            Log.d("IMAGE","IMAGE CATCH " + e.toString());
        }

        return null;
    }

    @Override
    public void onBackPressed()
    {
        //if overlay open, close
        if(overlay.getVisibility() == View.VISIBLE)
        {
            overlay.setVisibility(View.INVISIBLE);
            fab.setClickable(true);
            adapter.notifyDataSetChanged();
        }
        else
        {
            super.onBackPressed();
        }
    }
}