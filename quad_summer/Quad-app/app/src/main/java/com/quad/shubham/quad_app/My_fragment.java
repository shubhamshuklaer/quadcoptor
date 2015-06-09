package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.view.ViewGroup.LayoutParams;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;



/**
 * Created by shubham on 6/6/15.
 */
public class My_fragment extends Fragment {
    private Element node;
    private int position;
    GridLayout layout;
    String title;
    int num_columns;
    int num_rows;

    public static My_fragment newInstance(int _position){
        My_fragment fragment=new My_fragment();
        Bundle args = new Bundle();
        args.putInt("position", _position);
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
        title=node.getAttribute("name");

        num_columns=Integer.parseInt(node.getAttribute("num_cols"));
        num_rows=Integer.parseInt(node.getAttribute("num_rows"));

        layout=new GridLayout(parent_activity);
        layout.setColumnCount(num_columns);
        layout.setRowCount(num_rows);

        //Using solution from http://stackoverflow.com/a/24035591
        //The getWidth() and getHeight() functions will return 0 inside the Fragment's onCreateView
        //cause parent's view is not created until all the things are added to the view
        layout.post(new Runnable() {
            @Override
            public void run() {
                Activity parent_activity = getActivity();
                if (parent_activity != null) {
                    NodeList childs = node.getChildNodes();
                    int full_width = ((Tuner) parent_activity).pager.getWidth();
                    int full_height = ((Tuner) parent_activity).pager.getHeight();

                    for (int i = 0; i < childs.getLength(); i++) {
                        Node temp_node = childs.item(i);
                        if (temp_node.getNodeType() == Node.ELEMENT_NODE) {
                            Element temp_element = (Element) temp_node;
                            int element_start_cols = Integer.parseInt(temp_element.getAttribute("start_col"));
                            int element_start_rows = Integer.parseInt(temp_element.getAttribute("start_row"));
                            int element_num_cols = Integer.parseInt(temp_element.getAttribute("num_cols"));
                            int element_num_rows = Integer.parseInt(temp_element.getAttribute("num_rows"));
                            GridLayout.LayoutParams element_layout_params = new GridLayout.LayoutParams(
                                    GridLayout.spec(element_start_cols, element_num_cols), GridLayout.spec(element_start_rows, element_num_rows));

                            element_layout_params.height = full_height * element_num_cols / num_columns;
                            element_layout_params.width = full_width * element_num_rows / num_rows;

                            if ("graph".equals(temp_element.getNodeName())) {
                                GraphView temp_graph_view = new GraphView(parent_activity);
                                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                                        new DataPoint(0, 1),
                                        new DataPoint(1, 5),
                                        new DataPoint(2, 3),
                                        new DataPoint(3, 2),
                                        new DataPoint(4, 6)
                                });
                                temp_graph_view.setLayoutParams(element_layout_params);
                                temp_graph_view.addSeries(series);
                                layout.addView(temp_graph_view, element_layout_params);

                            }else if("slider".equals(temp_element.getNodeName())){
                                My_slider temp_seek_bar=new My_slider(parent_activity);
                                temp_seek_bar.set_direction(My_slider.Direction.VERTICAL);
                                temp_seek_bar.setLayoutParams(element_layout_params);
                                layout.addView(temp_seek_bar,element_layout_params);
                            }
                        }
                    }
                }
            }
        });

        return layout;
    }


}

