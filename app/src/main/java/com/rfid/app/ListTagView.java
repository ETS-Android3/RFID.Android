package com.rfid.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListTagView extends BaseAdapter {
    private List<Product> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public ListTagView(Context aContext,  List<Product> listData) {
        this.context = aContext;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }
    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listtag_items, null);
            holder = new ViewHolder();
            holder.productId = (TextView) convertView.findViewById(R.id.TvProductId);
            holder.tagId = (TextView) convertView.findViewById(R.id.TvTagUii);
            holder.color = (TextView) convertView.findViewById(R.id.TvColor);
            holder.name = (TextView) convertView.findViewById(R.id.TvName);
            holder.stock = (TextView) convertView.findViewById(R.id.TvStock);
            holder.count = (TextView) convertView.findViewById(R.id.TvTagCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = this.listData.get(i);
        holder.productId.setText(product.getProductId());
        holder.tagId.setText(product.getTagId());
        holder.name.setText(product.getName());
        holder.stock.setText(String.valueOf(product.getStock()));
        holder.count.setText(String.valueOf(product.getCount()));
        holder.color.setText(product.getColor());

        return convertView;
    }

    static class ViewHolder {
        TextView productId;
        TextView tagId;
        TextView name;
        TextView stock;
        TextView count;
        TextView color;

    }

}
