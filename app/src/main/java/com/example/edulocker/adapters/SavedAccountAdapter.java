package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.R;
import com.example.edulocker.models.SavedAccount;

import java.util.List;

public class SavedAccountAdapter extends RecyclerView.Adapter<SavedAccountAdapter.VH> {

    public interface Listener {
        void onSwitch(SavedAccount account);
        void onEditAlias(SavedAccount account);
        void onDelete(SavedAccount account);
    }

    private final List<SavedAccount> accounts;
    private final String activeUid;
    private final Listener listener;

    public SavedAccountAdapter(List<SavedAccount> accounts, String activeUid, Listener listener) {
        this.accounts = accounts;
        this.activeUid = activeUid;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_account, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SavedAccount acc = accounts.get(position);
        boolean isActive = acc.getUid().equals(activeUid);

        h.tvDisplayName.setText(acc.getDisplayName());
        h.tvEmail.setText(acc.getEmail());
        h.tvRoleLabel.setText(capitalize(acc.getRole()) + " Account");
        h.tvRoleBadge.setText(roleLetter(acc.getRole()));

        String alias = acc.getAlias();
        h.tvAliasHint.setText((alias != null && !alias.isEmpty())
                ? "Alias: " + alias : "No alias set");

        if (isActive) {
            h.tvActiveBadge.setVisibility(View.VISIBLE);
            h.btnSwitch.setVisibility(View.GONE);
        } else {
            h.tvActiveBadge.setVisibility(View.GONE);
            h.btnSwitch.setVisibility(View.VISIBLE);
            h.btnSwitch.setOnClickListener(v -> listener.onSwitch(acc));
        }

        h.btnEditAlias.setOnClickListener(v -> listener.onEditAlias(acc));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(acc));
    }

    @Override public int getItemCount() { return accounts.size(); }

    private String roleLetter(String role) {
        if (role == null) return "?";
        switch (role) {
            case "government": return "G";
            case "school":     return "O";
            case "teacher":    return "F";
            case "parent":     return "P";
            case "student":    return "S";
            default:           return role.substring(0, 1).toUpperCase();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        if ("school".equals(s)) return "Organisation";
        if ("teacher".equals(s)) return "Faculty";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoleBadge, tvDisplayName, tvEmail, tvRoleLabel;
        TextView tvActiveBadge, tvAliasHint, btnEditAlias, btnDelete, btnSwitch;

        VH(View v) {
            super(v);
            tvRoleBadge    = v.findViewById(R.id.tv_role_badge);
            tvDisplayName  = v.findViewById(R.id.tv_display_name);
            tvEmail        = v.findViewById(R.id.tv_email);
            tvRoleLabel    = v.findViewById(R.id.tv_role_label);
            tvActiveBadge  = v.findViewById(R.id.tv_active_badge);
            btnSwitch      = v.findViewById(R.id.btn_switch);
            tvAliasHint    = v.findViewById(R.id.tv_alias_hint);
            btnEditAlias   = v.findViewById(R.id.btn_edit_alias);
            btnDelete      = v.findViewById(R.id.btn_delete);
        }
    }
}
