package edu.berkeley.boss;

import edu.berkeley.boss.LocActivity.LocNode;
import edu.berkeley.boss.R;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;

public class LocTreeViewAdapter extends AbstractTreeViewAdapter<LocNode> {

  public LocTreeViewAdapter(Activity activity,
      TreeStateManager<LocNode> treeStateManager, int numberOfLevels) {
    super(activity, treeStateManager, numberOfLevels);
  }

  @Override
  public long getItemId(int position) {
    return getTreeId(position).id;
  }
  
  @Override
  public LocNode getItem(final int position) {
      return getTreeId(position);
  }

  @Override
  public View getNewChildView(TreeNodeInfo<LocNode> treeNodeInfo) {
    final LinearLayout viewLayout = (LinearLayout) getActivity()
        .getLayoutInflater().inflate(R.layout.loc_tree_node, null);
    return updateView(viewLayout, treeNodeInfo);
  }

  @Override
  public View updateView(View view, TreeNodeInfo<LocNode> treeNodeInfo) {
    final LinearLayout viewLayout = (LinearLayout) view;

    final TextView semTextView = (TextView) viewLayout
        .findViewById(R.id.loc_tree_node_semantic);
    semTextView.setText(treeNodeInfo.getId().semantic);

    final TextView zoneTextView = (TextView) viewLayout
        .findViewById(R.id.loc_tree_node_zone);
    zoneTextView.setText(treeNodeInfo.getId().zone);

    return viewLayout;
  }
}
