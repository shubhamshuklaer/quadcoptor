package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by shubham on 6/6/15.
 */
public class My_fragment extends Fragment {
    private Element node;
    private int position;
    GridLayout layout;
    String title;

    public static My_fragment newInstance(int _position){
        My_fragment fragment=new My_fragment();
        Bundle args = new Bundle();
        args.putInt("position",_position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position=getArguments().getInt("position");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View temp_view=super.onCreateView(inflater, container, savedInstanceState);

        Activity parent_activity=getActivity();
        if(parent_activity==null){
            return temp_view;
        }

        node=(Element)((Tuner)parent_activity).get_tab_node(position);
        title=node.getNodeName();

        ScrollView view=new ScrollView(parent_activity);

        NodeList childs=node.getChildNodes();

        int num_columns=Integer.parseInt(node.getAttribute("num_cols"));
        int num_rows=Integer.parseInt(node.getAttribute("num_rows"));

        layout=new GridLayout(parent_activity);
        layout.setColumnCount(num_columns);
        layout.setRowCount(num_rows);

        for(int i=0;i<childs.getLength();i++){
            Node temp_node=childs.item(i);
            if(temp_node.getNodeType()==Node.ELEMENT_NODE) {
                Element temp_element = (Element) temp_node;
                int element_cols = Integer.parseInt(temp_element.getAttribute("num_cols"));
                int element_rows = Integer.parseInt(temp_element.getAttribute("num_rows"));
                TextView temp_text_view = new TextView(parent_activity);
                temp_text_view.setText(title);
                layout.addView(temp_text_view);
            }
        }
        view.addView(layout);
        return view;
    }
}