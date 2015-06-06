package com.quad.shubham.quad_app;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.w3c.dom.Node;

/**
 * Created by shubham on 6/6/15.
 */
public class My_fragment extends Fragment {
    private String title;
    private Node node;

    public static My_fragment newInstance(Node _node){
        My_fragment fragment=new My_fragment();
        fragment.node=_node;
        return fragment;
    }

    public CharSequence get_title(){
        return "Hello";
    }
}