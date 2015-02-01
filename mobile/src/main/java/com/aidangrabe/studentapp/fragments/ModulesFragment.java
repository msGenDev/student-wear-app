package com.aidangrabe.studentapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aidangrabe.common.model.Module;
import com.aidangrabe.studentapp.R;
import com.aidangrabe.studentapp.fragments.base.MenuFragment;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

/**
 * Created by aidan on 31/01/15.
 *
 */
public class ModulesFragment extends MenuFragment {

    private ArrayAdapter<MenuItem> mAdapter;
    private ArrayList<Module> mModules;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mModules = new ArrayList<>();
        mAdapter = new ArrayAdapter<MenuItem>(getActivity(), android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                MenuItem item = getItem(position);

                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setText(item.getTitle());

                return view;
            }
        };

        getAllModules();
        setListAdapter(mAdapter);
        refreshList();

    }

    private void showConfirmationDialog(DialogInterface.OnClickListener okListener) {

        new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to delete this module?")
                .setPositiveButton("Yes", okListener)
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int position, long id) {
                final Module module = mModules.get(position);
                showConfirmationDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        module.delete();
                        getAllModules();
                        refreshList();
                    }
                });
                return true;
            }
        });

        createFab(view);

    }

    private void createFab(View view) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.fab_new_layout, (ViewGroup) view);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabNewModuleClicked();
            }
        });

        fab.attachToListView(getListView());

    }

    // called when the FAB for creating a new module is clicked
    private void onFabNewModuleClicked() {

    }

    private void getAllModules() {

        mModules.clear();
        mModules.addAll(Module.listAll(Module.class));

    }

    private void refreshList() {
        mAdapter.clear();

        for (Module module : mModules) {
            mAdapter.add(new MenuItem(module.getName(), null, null, null));
        }

        mAdapter.notifyDataSetChanged();
    }

    private void createNewModule(String title) {
        Module module = new Module(title);
        module.save();
        getAllModules();
        refreshList();
    }

}
