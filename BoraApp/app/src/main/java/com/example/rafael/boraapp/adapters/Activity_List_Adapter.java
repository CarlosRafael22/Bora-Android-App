package com.example.rafael.boraapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rafael.boraapp.R;
import com.example.rafael.boraapp.models.Activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

/**
 * Created by rafael on 29/05/16.
 */
public class Activity_List_Adapter extends BaseAdapter {


    Context context;
    private ArrayList<Activity> activities_list;

    TextView titleTV, authorTV, categoryTV, dateTV, placeTV;


    static class ViewHolder{
        public TextView titleTV, authorTV, categoryTV, dateTV, placeTV;

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    public Activity_List_Adapter(Context context, ArrayList<Activity> activities) {
        //super();
        this.context = context;
        this.activities_list = activities;
    }

    @Override
    public int getCount() {
        return activities_list.size();
    }

    @Override
    public Object getItem(int position) {
        return activities_list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        //SE a view for nula a gnt infla o layout e pega cada TextView de dentro
        //Se nao for ele so vai atualizar cada TextView
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_activities_list_item, null);

            // configure view holder
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.titleTV = (TextView) convertView.findViewById(R.id.activity_title);
            viewHolder.authorTV = (TextView) convertView.findViewById(R.id.activity_author);
            viewHolder.authorTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, viewHolder.authorTV.getText(), Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.categoryTV = (TextView) convertView.findViewById(R.id.activity_category);
            viewHolder.dateTV = (TextView) convertView.findViewById(R.id.activity_date);
            viewHolder.placeTV = (TextView) convertView.findViewById(R.id.activity_place);
            convertView.setTag(viewHolder);

        }

        // fill data
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.titleTV.setText(activities_list.get(position).getTitle());
        //Vou mostrar so o ID agora, dps vou ter que fazer algo pra mostrar o nome do cara e poder linkar pra mostrar uma VIew com o Profile dele
        viewHolder.authorTV.setText(activities_list.get(position).getAuthor_id());
        viewHolder.categoryTV.setText(activities_list.get(position).getCategory());

        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
        String activity_date;
        if(activities_list.get(position).getDate() == null){
            activity_date = "A definir";
        }else{
            activity_date = sdf2.format(activities_list.get(position).getDate());
        }

        viewHolder.dateTV.setText(activity_date);
        viewHolder.placeTV.setText(activities_list.get(position).getPlace());

        return convertView;
    }
}


