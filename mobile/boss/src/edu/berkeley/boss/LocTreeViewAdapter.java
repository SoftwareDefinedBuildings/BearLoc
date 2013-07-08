package edu.berkeley.boss;

import java.util.Arrays;

import edu.berkeley.boss.R;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;

public class LocTreeViewAdapter extends AbstractTreeViewAdapter<Long> {

  private final Activity mActivity;

  private final OnClickListener mOnClick = new OnClickListener() {
    @Override
    public void onClick(View v) {
      // TODO Inform mActivity to change to Map View
    }
  };

  public LocTreeViewAdapter(Activity activity,
      TreeStateManager<Long> treeStateManager, int numberOfLevels) {
    super(activity, treeStateManager, numberOfLevels);
    mActivity = activity;
  }

  @Override
  public long getItemId(int position) {
    return getTreeId(position);
  }

  @Override
  public View getNewChildView(TreeNodeInfo<Long> treeNodeInfo) {
    final LinearLayout viewLayout = (LinearLayout) getActivity()
        .getLayoutInflater().inflate(R.layout.loc_tree_node, null);
    return updateView(viewLayout, treeNodeInfo);
  }

  @Override
  public View updateView(View view, TreeNodeInfo<Long> treeNodeInfo) {
    final LinearLayout viewLayout = (LinearLayout) view;

    final TextView levelView = (TextView) viewLayout
        .findViewById(R.id.loc_tree_node_level);
    levelView.setText(Integer.toString(treeNodeInfo.getLevel()));

    final TextView descriptionView = (TextView) viewLayout
        .findViewById(R.id.loc_tree_node_description);
    descriptionView.setText(getDescription(treeNodeInfo.getId()));

    final Button correctButton = (Button) viewLayout
        .findViewById(R.id.loc_tree_node_report_button);
    correctButton.setOnClickListener(mOnClick);

    return viewLayout;
  }

  private String getDescription(final long id) {
    // TODO get from location
    final Integer[] hierarchy = getManager().getHierarchyDescription(id);
    return "Node " + id + Arrays.asList(hierarchy);
  }

}
