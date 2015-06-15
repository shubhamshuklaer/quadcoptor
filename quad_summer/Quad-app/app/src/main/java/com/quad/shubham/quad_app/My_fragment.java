package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;


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
    ArrayList<Graph_data_receiver> receivers;

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
        receivers=new ArrayList<Graph_data_receiver>();
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

                            element_layout_params.height = full_height * element_num_rows / num_columns;
                            element_layout_params.width = full_width * element_num_cols / num_rows;

                            if ("graph".equals(temp_element.getNodeName())) {
                                GraphView temp_graph_view = new GraphView(parent_activity);

                                NodeList graph_childs=temp_element.getChildNodes();

                                for(int j=0;j<graph_childs.getLength();j++) {
                                    Node graph_child=graph_childs.item(j);
                                    if(graph_child.getNodeType()==Node.ELEMENT_NODE && "series".equals(graph_child.getNodeName())) {
                                        Element graph_child_element=(Element)graph_child;

                                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
                                        String prefix = graph_child_element.getAttribute("name").toString();
                                        int color = My_fragment.try_parse_hex_int(graph_child_element.getAttribute("color").toString());

                                        Log.e("normal", Integer.toString(color));

                                        receivers.add(new Graph_data_receiver(parent_activity, series, prefix));
                                        series.setTitle(prefix);
                                        series.setColor(color);

                                        temp_graph_view.addSeries(series);
                                    }
                                }

                                temp_graph_view.getViewport().setXAxisBoundsManual(true);
                                temp_graph_view.getViewport().setMinX(-1000000);
                                temp_graph_view.getViewport().setMaxX(1000000);
                                temp_graph_view.getViewport().setScalable(true);
                                temp_graph_view.getViewport().setScrollable(true);
                                temp_graph_view.getLegendRenderer().setVisible(true);

                                layout.addView(temp_graph_view, element_layout_params);

                            }else if("slider".equals(temp_element.getNodeName())){
                                My_slider temp_seek_bar;
                                String param_name=temp_element.getAttribute("param_name").toString();
                                if(element_num_rows>element_num_cols)
                                    temp_seek_bar=My_slider.new_instance(parent_activity, My_slider.Direction.VERTICAL,param_name);
                                else
                                    temp_seek_bar=My_slider.new_instance(parent_activity, My_slider.Direction.HORIZONTAL,param_name);
                                layout.addView(temp_seek_bar,element_layout_params);
                            }
                        }
                    }

                    for(int i=0;i<receivers.size();i++)
                        receivers.get(i).register_receiver();
                }
            }
        });



        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        for(int i=0;i<receivers.size();i++)
            receivers.get(i).unregister_receiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        for(int i=0;i<receivers.size();i++)
            receivers.get(i).register_receiver();
    }

    public static int try_parse_hex_int(String str){
        try {
            //The max value is 0xFFFFFFFF whose value 4294967295 is outside int range
            //So we get NumberFormatException if we use Integer.ParseInt(str,16)
            //Sol parse using long and then cast it to int it will send it to the negative side
            //of int just like we want according to the color class
            //http://developer.android.com/reference/android/graphics/Color.html
            return (int)Long.parseLong(str,16);
        } catch(NumberFormatException nfe) {
            return 0;
        }
    }
}

