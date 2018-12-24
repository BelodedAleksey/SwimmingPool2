package beloded.alexey.com.swimmingpool;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class TimeAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<TimeField> objects;

    TimeAdapter(Context context, ArrayList<TimeField> fields){
        ctx = context;
        objects = fields;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = inflater.inflate(R.layout.item, parent, false);
        }

        TimeField t = getField(position);
        return null;
    }

    TimeField getField(int position){
        return ((TimeField) getItem(position));
    }

    
}
