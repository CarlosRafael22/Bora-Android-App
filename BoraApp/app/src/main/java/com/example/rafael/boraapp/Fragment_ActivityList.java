package com.example.rafael.boraapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.rafael.boraapp.adapters.Activity_List_Adapter;
import com.example.rafael.boraapp.models.Activity;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_ActivityList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_ActivityList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_ActivityList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;
    private ListView listView;

    private DatabaseHandler databaseHandler;
    private ArrayList<Activity> activities_list = new ArrayList<>();
    Activity_List_Adapter adapter;

    public Fragment_ActivityList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_ActivityList.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_ActivityList newInstance(String param1, String param2) {
        Fragment_ActivityList fragment = new Fragment_ActivityList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

//        databaseHandler = DatabaseHandler.getDatabaseHandler(getActivity());
//
//        getActivitiesFromDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_activities_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.activities_listview);

        databaseHandler = DatabaseHandler.getDatabaseHandler(getActivity());
        getActivitiesFromDatabase();

        //Log.v("FRAG ACT LIST", activities_list.get(0).getTitle());
        adapter = new Activity_List_Adapter(getActivity(), activities_list);
        listView.setAdapter(adapter);

        Log.i("FRAG ACT LIST", "ON CREATE VIEW");
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.i("FRAG ACT LIST", "SET USER VISIBLE");
        if(isVisibleToUser){
            getActivitiesFromDatabase();
            Log.i("FRAG ACT LIST", "IS VISIBLE");
            adapter.notifyDataSetChanged();
            //listView.invalidateViews();
            Log.i("FRAG ACT LIST", String.valueOf(adapter.getCount()));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //To atualizando aqui o ArrayList pq qd a Activity terminou de ser criada provavelmente ja vai ter terminado o AsyncTask de add as activities no BD tb
        Log.i("FRAG ACT LIST", "ON ACTIVITY CREATED");
        getActivitiesFromDatabase();
        adapter.notifyDataSetChanged();
        Log.i("FRAG ACT LIST", String.valueOf(adapter.getCount()));

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivitiesFromDatabase();;
        Log.i("FRAG ACT LIST", "ON RESUME");
        adapter.notifyDataSetChanged();
        Log.i("FRAG ACT LIST", String.valueOf(adapter.getCount()));
    }

    public void getActivitiesFromDatabase(){

        Log.i("FRAG ACT LIST", "Get list");
        activities_list.clear();
        activities_list.addAll(databaseHandler.getAllActivities());
        //activities_list = databaseHandler.getAllActivities();
        Log.i("FRAG ACT LIST", String.valueOf(activities_list.size()));

    }


//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
