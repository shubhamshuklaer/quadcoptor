package com.quad.shubham.quad_app;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by shubham on 11/6/15.
 */
public class Node_view_holder extends TreeNode.BaseNodeViewHolder<Node_view_holder.IconTreeItem> {

    public Node_view_holder(Context context){
        super(context);
    }
    @Override
    public View createNodeView(TreeNode treeNode, IconTreeItem iconTreeItem) {
        TextView view=new TextView(context);
        view.setText(iconTreeItem.text);
        return view;
    }

    public static class IconTreeItem {
        public int icon;
        public String text;
    }
}
