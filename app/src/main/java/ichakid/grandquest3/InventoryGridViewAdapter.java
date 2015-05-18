package ichakid.grandquest3;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by User on 4/23/2015.
 */
public class InventoryGridViewAdapter extends ArrayAdapter<Item>{
    Context context;
    int layoutResourceId;
    ArrayList<Item> data = new ArrayList<>();

    public InventoryGridViewAdapter(Context context, int layoutResourceId, ArrayList<Item> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            holder.container = (FrameLayout) row.findViewById(R.id.container);
            holder.itemId = (TextView) row.findViewById(R.id.item_id);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }
        Item item = data.get(position);
        if (item.getNumberOf() >= 0) {
            holder.txtTitle.setText("" + item.getNumberOf());
        } else {
            holder.txtTitle.setText(item.getTitle());
        }
        holder.imageItem.setImageBitmap(item.getImage());
        if (item.getId() >= 0){
            holder.itemId.setText("" + item.getId());
        }
        if(item.getContainer()) {
            holder.container.setBackgroundResource(R.drawable.empty);
            if (!(getContext().toString().contains("TradeboxActivity")) && !(getContext().toString().contains("OffersboxActivity")))
            holder.imageItem.setOnTouchListener(new MyTouchListener());
        }
        if(item.isTargetDrop()){
            holder.container.setOnDragListener(new MyDragListener());
        }
        return row;
    }

    static class ItemHolder {
        TextView txtTitle;
        ImageView imageItem;
        FrameLayout container;
        TextView itemId;
    }

    private class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    private class MyDragListener implements View.OnDragListener {
        Drawable enterShape = context.getResources().getDrawable(R.drawable.empty);
        Drawable normalShape = context.getResources().getDrawable(R.drawable.empty);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    FrameLayout container = (FrameLayout) v;
                    // Dropped, reassign View to ViewGroup
                    ImageView view = (ImageView) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();

                    if ((ViewGroup) container != owner) {
                        ImageView oldView = new ImageView(context);
                        oldView.setImageDrawable(view.getDrawable());
                        owner.removeView(view);
                        owner.addView(oldView);

                        String id = "" + ((TextView) owner.getChildAt(0)).getText();
                        TextView number = (TextView) ((ViewGroup) owner.getParent()).getChildAt(1);
                        String activity = getContext().toString();
                        if ((!activity.contains("InventoryActivity")) || ((number.getText().toString() != "") && (Integer.parseInt(number.getText().toString()) >= 3))) {
                            //Replace item image
                            container.removeView((ImageView) container.getChildAt(1));
                            container.addView(view);
                            view.setVisibility(View.VISIBLE);
                            //Decrease number of items by 3
                            if (activity.contains("InventoryActivity")) {
                                number.setText("" + (Integer.parseInt(number.getText().toString()) - 3));
                                View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                                GridView gridview = (GridView) rootView.findViewById(R.id.gridView1);
                                System.out.println("Container = " + ((TextView) container.getChildAt(0)).getText());
                                System.out.println(Integer.parseInt(((TextView) container.getChildAt(0)).getText().toString()));
                                int idOldItem = Integer.parseInt(((TextView) container.getChildAt(0)).getText().toString());
                                if (idOldItem >= 0) {
                                    TextView numOldItem = (TextView) ((View) gridview.getChildAt(idOldItem)).findViewById(R.id.item_text);
                                    System.out.println((View) gridview.getChildAt(Integer.parseInt(((TextView) container.getChildAt(0)).getText().toString())));
                                    numOldItem.setText("" + (Integer.parseInt(numOldItem.getText().toString()) + 3));
                                }
                                if (InventoryActivity.item1 >= 0) {
                                    InventoryActivity.item2 = Integer.parseInt(id);
                                } else {
                                    InventoryActivity.item1 = Integer.parseInt(id);
                                }
                            }
                            ((TextView) container.findViewById(R.id.item_id)).setText(id);

                            //Replace item name
                            TextView itemname = (TextView) ((ViewGroup) container.getParent()).getChildAt(1);
                            itemname.setText("" + Item.findNameById(Integer.parseInt(id)));

                            if (activity.contains("OfferActivity")) {
                                View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                                NumberPicker np = (NumberPicker) rootView.findViewById(R.id.numberPicker);
                                np.setMaxValue(Integer.parseInt(number.getText().toString()));
                                np.setMinValue(1);

                                NumberPicker np2 = (NumberPicker) rootView.findViewById(R.id.numberPicker2);
                                np2.setMaxValue(99);
                                np2.setMinValue(1);
                            }
                            if (activity.contains("FindActivity")) {
                                View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                                ViewGroup findItem = (ViewGroup) rootView.findViewById(R.id.gridView2);
                                String findId = ((TextView) findItem.findViewById(R.id.item_id)).getText().toString();
                                FindActivity.item = Integer.parseInt(findId);
                            }
                        }
                        oldView.setOnTouchListener(new MyTouchListener());
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }
}
