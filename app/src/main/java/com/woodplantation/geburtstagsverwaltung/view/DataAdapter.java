package com.woodplantation.geburtstagsverwaltung.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.util.DateUtil;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class DataAdapter extends ListAdapter<Entry, DataAdapter.DataViewHolder> {

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView remaining;
        private final TextView notes;
        private final TextView birthday;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.viewholder_name);
            remaining = itemView.findViewById(R.id.viewholder_remaining);
            notes = itemView.findViewById(R.id.viewholder_notes);
            birthday = itemView.findViewById(R.id.viewholder_birthday);
        }
    }

    private final Context context;

    @Inject
    public DataAdapter(@ApplicationContext Context context) {
        super(new DiffUtil.ItemCallback<Entry>() {
            @Override
            public boolean areItemsTheSame(@NonNull Entry oldItem, @NonNull Entry newItem) {
                return Objects.equals(oldItem.id, newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Entry oldItem, @NonNull Entry newItem) {
                return oldItem.birthday.equals(newItem.birthday) &&
                        oldItem.firstName.equals(newItem.firstName) &&
                        oldItem.lastName.equals(newItem.lastName) &&
                        oldItem.notes.equals(newItem.notes) &&
                        oldItem.ignoreYear == newItem.ignoreYear;
            }
        });
        this.context = context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        Entry data = getItem(position);
        holder.name.setText(data.getFullName());
        holder.remaining.setText(DateUtil.getRemainingWithAge(context, data.birthday));
        holder.notes.setText(data.notes);
        holder.birthday.setText(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(data.birthday));
    }


}
