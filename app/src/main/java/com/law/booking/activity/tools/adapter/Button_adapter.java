package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Guess.nearby_activity;
import com.law.booking.activity.MainPageActivity.Provider.hmua;
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

        switch (button_class.getName()){
            case "Nearby Firms":
                viewHolder.icons.setImageResource(R.mipmap.location_icon);
                break;
            case "Corporate Law":
                viewHolder.icons.setImageResource(R.mipmap.group_icon);
                break;
            case "Criminal Law":
                viewHolder.icons.setImageResource(R.mipmap.posas);
                break;
            case "Tax Law":
                viewHolder.icons.setImageResource(R.mipmap.tax);
                break;
            case "Human Rights Law":
                viewHolder.icons.setImageResource(R.mipmap.human_rights_icon);
                break;
            case "Contract Law":
                viewHolder.icons.setImageResource(R.mipmap.building_icon);
                break;
            case "Family Law":
                viewHolder.icons.setImageResource(R.mipmap.family_icon);
                break;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = null;
                    switch (button_class.getName()) {
                        case "Corporate Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Corporate Law");
                            intent.putExtra("isCorporate", true);
                            context.startActivity(intent);
                            break;

                        case "Criminal Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Criminal Law");
                            intent.putExtra("isCriminal", true);
                            context.startActivity(intent);
                            break;

                        case "Tax Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Tax Law");
                            intent.putExtra("isTax", true);
                            context.startActivity(intent);
                            break;

                        case "Human Rights Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Human Rights Law");
                            intent.putExtra("isHumanRights", true);
                            context.startActivity(intent);
                            break;

                        case "Family Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Family");
                            intent.putExtra("isFamily", true);
                            context.startActivity(intent);
                            break;

                        case "Contract Law":
                            intent = new Intent(context, hmua.class);
                            intent.putExtra("title", "Contract Law");
                            intent.putExtra("isContract", true);
                            context.startActivity(intent);
                            break;
                        case "Nearby Firms":
                            intent = new Intent(context, nearby_activity.class);
                            context.startActivity(intent);
                            break;
                    }

            }

        });
    }

    @Override
    public int getItemCount() {
        return button_list.size();
    }
}