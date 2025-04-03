package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.squareimagebutton.ImageButton;
import com.example.squareimagebutton.ImageButtonConfig;
import com.example.squareimagebutton.SquareImageButton;
import com.law.booking.R;
import com.law.booking.activity.MainPageActivity.Guess.nearby_activity;
import com.law.booking.activity.MainPageActivity.Provider.hmua;
import com.law.booking.activity.tools.Model.Button_class;
import com.law.booking.activity.tools.Model.Image_harcoded_url;

import java.util.List;

public class Button_adapter extends RecyclerView.Adapter<Button_adapter.ViewHolder> {
    private List<Button_class> button_list;
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private SquareImageButton icons;
        public ViewHolder(View view) {
            super(view);
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
        switch (button_class.getName()){
            case "Nearby Firms":
                setImage(Image_harcoded_url.Nearby_Firms,viewHolder,"Nearby Firms");
                break;
            case "Corporate Law":
                setImage(Image_harcoded_url.Corporate_Law,viewHolder,"Corporate Law");
                break;
            case "Criminal Law":
                setImage(Image_harcoded_url.Criminal_Law,viewHolder,"Criminal Law");
                break;
            case "Tax Law":
                setImage(Image_harcoded_url.Tax_Law,viewHolder,"Tax Law");
                break;
            case "Human Rights Law":
                setImage(Image_harcoded_url.Human_Rights_Law,viewHolder,"Human Rights Law");
                break;
            case "Contract Law":
                setImage(Image_harcoded_url.Contract_Law,viewHolder,"Contract Law");
                break;
            case "Family Law":
                setImage(Image_harcoded_url.Family_Law,viewHolder,"Family Law");
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

    private void setImage(String image_url, ViewHolder viewHolder,String mainTitle) {
        viewHolder.icons.init(new ImageButtonConfig
                        .ImageButtonConfigBuilder(context)
                        .mainTitleSize(13)
                        .mainTitletypeface(ResourcesCompat.getFont(context, R.font.century_gothic))
                        .subTitletypeface(ResourcesCompat.getFont(context, R.font.century_gothic))
                        .build(),
                new ImageButton(image_url,
                        mainTitle,
                      ""));

    }

    @Override
    public int getItemCount() {
        return button_list.size();
    }
}