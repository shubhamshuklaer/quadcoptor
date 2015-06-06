package com.quad.shubham.quad_app;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shubham on 6/6/15.
 */
public class My_pager_adapter extends FragmentPagerAdapter {

    ArrayList<My_fragment> fragment_list;

    public My_pager_adapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        fragment_list=new ArrayList<My_fragment>();
    }

    public void set_fragments(NodeList node_list){
        for(int i=0;i<node_list.getLength();i++){
            My_fragment temp_fragment=My_fragment.newInstance(node_list.item(i));
            fragment_list.add(temp_fragment);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragment_list.get(position);
    }

    @Override
    public int getCount() {
        return fragment_list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragment_list.get(position).get_title();
    }
}
