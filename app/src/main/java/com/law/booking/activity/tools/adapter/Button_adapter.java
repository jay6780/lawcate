package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.R;
import com.law.booking.activity.tools.Model.Button_class;

import java.util.List;

public class Button_adapter extends RecyclerView.Adapter<Button_adapter.ViewHolder> {
    private List<Button_class> button_list;
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView icons;
        public ViewHolder(View view) {
            super(view);
           name = view.findViewById(R.id.name);
           icons = view.findViewById(R.id.icon_btn);

        }

    }


    public Button_adapter(Context context, List<Button_class> button_list) {
       this.context = context;
       this.button_list = button_list;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.button_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Button_class button_class = button_list.get(position);
        viewHolder.name.setText(button_class.getName());
        if(button_class.getName().equals("Nearby Firms")){
            viewHolder.icons.setImageResource(R.mipmap.location_icon);

        }else if(button_class.getName().equals("Corporate Law")){
            viewHolder.icons.setImageResource(R.mipmap.group_icon);

        }else if(button_class.getName().equals("Criminal Law")){
            viewHolder.icons.setImageResource(R.mipmap.posas);

        }else if(button_class.getName().equals("Tax Law")){
            viewHolder.icons.setImageResource(R.mipmap.tax);

        }else if(button_class.getName().equals("Human Rights Law")){
            viewHolder.icons.setImageResource(R.mipmap.human_rights_icon);

        }else if(button_class.getName().equals("Contract Law")){
            viewHolder.icons.setImageResource(R.mipmap.building_icon);

        }else if(button_class.getName().equals("Family Law")){
            viewHolder.icons.setImageResource(R.mipmap.family_icon);

        }
    }

    @Override
    public int getItemCount() {
        return button_list.size();
    }
}