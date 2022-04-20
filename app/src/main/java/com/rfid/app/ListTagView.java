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
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listtag_items, null);
            holder = new ViewHolder();
            holder.productId = (TextView) convertView.findViewById(R.id.TvProductId);
            holder.tagId = (TextView) convertView.findViewById(R.id.TvTagUii);
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
        holder.stock.setText(product.getStock());
        holder.count.setText(product.getCount());

        return convertView;
    }


    // Find Image ID corresponding to the name of the image (in the directory mipmap).
    public int getMipmapResIdByName(String resName)  {
        String pkgName = context.getPackageName();
        // Return 0 if not found.
        int resID = context.getResources().getIdentifier(resName , "mipmap", pkgName);
        Log.i("CustomListView", "Res Name: "+ resName+"==> Res ID = "+ resID);
        return resID;
    }

    static class ViewHolder {
        TextView productId;
        TextView tagId;
        TextView name;
        TextView stock;
        TextView count;
    }

}
