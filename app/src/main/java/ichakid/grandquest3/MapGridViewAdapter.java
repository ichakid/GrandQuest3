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
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MapGridViewAdapter extends ArrayAdapter<Sqrchara> {
    Context context;
    int layoutResourceId;
    ArrayList<Sqrchara> data = new ArrayList<>();
    int poohPos;
    int w, h;

    public MapGridViewAdapter(Context context, int layoutResourceId, ArrayList<Sqrchara> data, int poohPos, int w, int h) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.poohPos = poohPos;
        this.w = w;
        this.h = h;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
            holder.container = (FrameLayout) row.findViewById(R.id.container);
            holder.itemId = (TextView) row.findViewById(R.id.item_id);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }
        Sqrchara sqrchara = data.get(position);
        holder.imageItem.setMaxHeight(holder.imageItem.getWidth());
        if (position == poohPos && !MapActivity.pooh) {
            holder.imageItem.setImageBitmap(Sqrchara.findBitmapById(context, 0));
            holder.imageItem.setOnTouchListener(new MyTouchListener());
            MapActivity.pooh = true;
        }
        if (sqrchara.getId() >= 0){
            holder.itemId.setText("" + sqrchara.getId());
        }
        holder.container.setBackgroundResource(R.drawable.tilegg);

        holder.container.setOnDragListener(new MyDragListener(position));
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
        Drawable enterShape = context.getResources().getDrawable(R.drawable.tilebkg);
        Drawable normalShape = context.getResources().getDrawable(R.drawable.tilegg);
        int position;

        public MyDragListener(int position){
            this.position = position;
        }

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

                        ImageView charashad = (ImageView) ((ViewGroup) container).findViewById(R.id.item_image);
                        charashad.setImageBitmap(Sqrchara.findBitmapById(context, 2));

                        JSONObject json = new JSONObject();
                        try {
                            json.put("method", "move");
                            json.put("token", LoginActivity.token);
                            MapActivity.moveX = position % w;
                            MapActivity.moveY = position / w;
                            json.put("x", MapActivity.moveX);
                            json.put("y", MapActivity.moveY);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MapActivity.mBoundService.sendMessage(json.toString());
                        MapActivity.move = true; MapActivity.pooh = false;
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }}


