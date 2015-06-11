package com.quad.shubham.quad_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

/**
 * Created by shubham on 5/6/15.
 */
public class Show_history extends Activity {
    AndroidTreeView tree_view;
    TreeNode root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root=TreeNode.root();
        Node_view_holder.IconTreeItem node_item= new Node_view_holder.IconTreeItem();

        node_item.text="parent";
        TreeNode parent_node=new TreeNode(node_item).setViewHolder(new Node_view_holder(Show_history.this));

        node_item.text="c1";
        TreeNode c1=new TreeNode(node_item).setViewHolder(new Node_view_holder(Show_history.this));
        node_item.text="c2";
        TreeNode c2=new TreeNode(node_item).setViewHolder(new Node_view_holder(Show_history.this));
        parent_node.addChildren(c1,c2);
        root.addChild(parent_node);
        tree_view= new AndroidTreeView(Show_history.this,root);
        setContentView(tree_view.getView());
    }
}
