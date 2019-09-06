package com.wmontgom85.flickrfindr.api.jsonadapter;

import android.util.Log;
import com.wmontgom85.flickrfindr.api.response.ImageSearchResponse;
import com.wmontgom85.flickrfindr.repo.model.FlickrImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FlickrJsonAdapter implements Parser {
    @Override
    public Object readFrom(String json) {
        try {
            JSONObject resp = new JSONObject(json);

            if (resp.has("photos") && resp.get("photos") instanceof JSONObject) {
                JSONObject photos = resp.getJSONObject("photos");

                ArrayList<FlickrImage> imagesList = new ArrayList<>();

                if (photos.has("photo") && photos.get("photo") instanceof JSONArray) {
                    JSONArray photosArray = photos.getJSONArray("photo");

                    for (int i = 0; i < photosArray.length(); ++i) {
                        JSONObject photo = photosArray.getJSONObject(i);

                        imagesList.add(new FlickrImage(
                            photo.getString("id"),
                            photo.getString("owner"),
                            photo.getString("secret"),
                            photo.getInt("server"),
                            photo.getInt("farm"),
                            photo.getString("title"),
                            photo.getInt("ispublic"),
                            photo.getInt("isfriend"),
                            photo.getInt("isfamily")
                        ));
                    }
                }

                return new ImageSearchResponse(
                    photos.getInt("page"),
                    photos.getInt("pages"),
                    photos.getInt("perpage"),
                    photos.getLong("total"),
                    imagesList
                );
            }
        } catch (JSONException e) {
            // there was an error parsing the json
            Log.d("FlickrJsonAdapter", String.format("parse error: %s", e.getMessage()));
        } catch (Throwable tx) {
            // there was an error parsing the json
            Log.d("FlickrJsonAdapter", String.format("exception: %s", tx.getMessage()));
        }

        return null;
    }
}
