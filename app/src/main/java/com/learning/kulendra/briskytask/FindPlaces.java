package com.learning.kulendra.briskytask;

/**
 * Created by kulendra on 14/9/17.
 */


        import java.io.BufferedInputStream;
        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.List;

        import org.apache.http.HttpResponse;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.util.ByteArrayBuffer;
        import org.json.JSONArray;
        import org.json.JSONObject;

        import android.app.ListActivity;
        import android.location.Location;
        import android.os.AsyncTask;
        import android.util.Log;
        import android.widget.ArrayAdapter;

        import javax.net.ssl.HttpsURLConnection;

public class FindPlaces extends ListActivity {
    ArrayList<GooglePlace> venuesList=new ArrayList<>();
    final String GOOGLE_KEY = "AIzaSyBpuugPE-SKwSyt5BN1QZNuGVlOdN_DEw8";

    private Location loc;
    String latitude;
    String longtitude;
    double radius=5000;
    String type="restaurant";
    String keyword="night";

    public ArrayList<GooglePlace> g1 = new ArrayList<>();

    public void getExecute(){
        g1=venuesList;
    }

    public void getExecuteInstance(double radius,Location l2)
    {
        this.radius=radius;
        this.loc=l2;
        latitude = String.valueOf(loc.getLatitude());
        longtitude = String.valueOf(loc.getLongitude());
        new googleplaces().execute();
    }

    public FindPlaces(Location l1,double radius){
        Log.d("FindPlacesConstructor","called");
        this.loc=l1;
        latitude = String.valueOf(loc.getLatitude());
        longtitude = String.valueOf(loc.getLongitude());
        this.radius=radius;
        new googleplaces().execute();
        Log.d("FindPlaces","venueSizeinConstructor"+venuesList.size());
    }

    private class googleplaces extends AsyncTask<Void,Void,String> {

        String temp;

        @Override
        protected String doInBackground(Void... arg0) {
            // make Call to the url
            temp = makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longtitude+"&radius="+radius+"&type="+type+"&keyword="+keyword+"&key=" + GOOGLE_KEY);

            //print the call in the console
            Log.d("doInBackground called","api called");
            return "";
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
        }

        public ArrayList getexecute(){
            return venuesList;
        }

        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {
                Log.d("FindPlaces","make call returned nothing");
            } else {
                venuesList = parseGoogleParse(temp);
                getexecute();
                Log.d("FindPlaces","venue List size:"+venuesList.size());
                List listTitle = new ArrayList();

                for (int i = 0; i < venuesList.size(); i++) {
                    Log.d("FindPlaces","lat"+venuesList.get(i).getL().getLatitude()+"long"+venuesList.get(i).getL().getLongitude());
                    listTitle.add(i, venuesList.get(i).getName() + "\nOpen Now: " + venuesList.get(i).getOpenNow() + "\n(" + venuesList.get(i).getCategory() + ")");
                }
            }
        }
    }

    public static String makeCall(String url) {

        Log.d("makecall starting","FindPlaces");
        String replyString = "";

        try {
            URL url1=new URL(url);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int responseCode=conn.getResponseCode();
            if(responseCode== HttpsURLConnection.HTTP_OK)
            {
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer baf = new ByteArrayBuffer(20);
                int current = 0;
                Log.d("make func beforeLoop","FindPlace");
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                replyString = new String(baf.toByteArray());
                Log.d("make function called","FindPlace");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(replyString);

        // trim the whitespaces
        return replyString.trim();
    }

    private static ArrayList parseGoogleParse(final String response) {

        ArrayList<GooglePlace> temp = new ArrayList<GooglePlace>();
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("results")) {

                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    GooglePlace poi = new GooglePlace();
                    if (jsonArray.getJSONObject(i).has("name")) {
                        poi.setName(jsonArray.getJSONObject(i).optString("name"));
                        poi.setRating(jsonArray.getJSONObject(i).optString("rating", " "));

                        if (jsonArray.getJSONObject(i).has("opening_hours")) {
                            if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").has("open_now")) {
                                if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").getString("open_now").equals("true")) {
                                    poi.setOpenNow("YES");
                                } else {
                                    poi.setOpenNow("NO");
                                }
                            }
                        } else {
                            poi.setOpenNow("Not Known");
                        }
                        if (jsonArray.getJSONObject(i).has("types")) {
                            JSONArray typesArray = jsonArray.getJSONObject(i).getJSONArray("types");

                            for (int j = 0; j < typesArray.length(); j++) {
                                poi.setCategory(typesArray.getString(j) + ", " + poi.getCategory());
                            }
                        }
                        if (jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").has("lat")){
                            String lat=jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                            String log=jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                            poi.setL(Double.parseDouble(lat),Double.parseDouble(log));
                        }
                    }
                    temp.add(poi);
                }
                Log.d("FindPlaces",""+temp.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
        return temp;

    }
}
