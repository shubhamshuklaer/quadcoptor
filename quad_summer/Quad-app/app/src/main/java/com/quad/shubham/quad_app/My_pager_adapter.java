package com.quad.shubham.quad_app;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

/**
 * Created by shubham on 6/6/15.
 */
public class My_pager_adapter extends FragmentStatePagerAdapter {

    NodeList node_list;

    public My_pager_adapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void set_fragments(NodeList _node_list){
        node_list=_node_list;
    }

    @Override
    public Fragment getItem(int position) {
        return My_fragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return node_list.getLength();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Element temp_element=(Element)node_list.item(position);
        return temp_element.getAttribute("name");
    }
}
