package com.hack.letsmeet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.hack.letsmeet.Friend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendsListAdapter extends ArrayAdapter<Friend> {

    public FriendsListAdapter(Context context, int resource, List<Friend> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if( convertView == null ){
            //We must create a View:
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1,  parent, false);
        }

        final Friend friend = getItem(position);

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(friend.name);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FriendsListAdapter", friend.name + " was clicked");
                JSONObject params = new JSONObject();
                try {
                    params.put("friendId", friend.id);

                    params.put("lat", 43.4667);

                    params.put("lon", -80.5333/*MapsActivity.userMarker.getPosition().longitude*/);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RestApi.getInstance().request(getContext(),"initiate" , RestApi.Method.POST, params, new Response.Listener() {
                    @Override
                    public void onResponse(Object o) {

                        JSONObject obj = (JSONObject) o;
                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        intent.putExtra("meeting", obj.toString());
                        getContext().startActivity(intent);
                    }
                });
            }
        });

        return convertView;
    }


}