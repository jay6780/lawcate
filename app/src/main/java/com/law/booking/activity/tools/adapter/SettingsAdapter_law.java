package com.law.booking.activity.tools.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.law.booking.R;
import com.law.booking.activity.tools.Model.Law_names;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.beppi.tristatetogglebutton_library.TriStateToggleButton;

public class SettingsAdapter_law extends RecyclerView.Adapter<SettingsAdapter_law.ViewHolder> {

    private List<Law_names> law_list;
    private Context context;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView law_name;
        private TriStateToggleButton toggle_switch;
        public ViewHolder(View view) {
            super(view);
            law_name = view.findViewById(R.id.law_name);
            toggle_switch = view.findViewById(R.id.settings_toggle);
        }

    }

    public SettingsAdapter_law(Context context,List<Law_names> law_list) {
        this.context = context;
        this.law_list = law_list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.law_settings_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Law_names law_names = law_list.get(position);
        viewHolder.law_name.setText(law_names.getLaw_name());
        viewHolder.toggle_switch.setToggleStatus(law_names.isOpen());
        viewHolder.toggle_switch.setOnToggleChanged((toggleStatus, booleanToggleStatus, toggleIntValue) -> {
            Map<String, Object> updates = new HashMap<>();
            String lawKey = "";
            switch (law_names.getLaw_name()) {
                case "Corporate Law":
                    lawKey = "isCorporate";
                    break;
                case "Criminal Law":
                    lawKey = "isCriminal";
                    break;
                case "Family Law":
                    lawKey = "isFamily";
                    break;
                case "Human Rights Law":
                    lawKey = "isHumanRights";
                    break;
                case "Tax Law":
                    lawKey = "isTax";
                    break;
                case "Contract Law":
                    lawKey = "isContract";
                    break;
                case "Online":
                    lawKey = "isOnline_book";
                    break;
                case "On Site":
                    lawKey = "isOnsite_book";
                    break;
            }

            if (!lawKey.isEmpty()) {
                updates.put(lawKey, booleanToggleStatus);
                savedLawsettings(updates, context);
            }
        });
    }

    private void savedLawsettings(Map<String, Object> updates,Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference adminRef = FirebaseDatabase.getInstance()
                    .getReference("Lawyer")
                    .child(user.getUid());

            adminRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Data update!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Saveddatafromservice", "Failed to save data: " + e.getMessage());
                    });
        }
    }


    @Override
    public int getItemCount() {
        return law_list.size();
    }
}
