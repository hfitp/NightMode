/*
 * Copyright (C) 2014 Ruikye's open source project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.awaysoft.nightlymode.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Preference Adapter
 *
 * @author ruikye
 * @since 2014
 */
public class PreferenceAdapter extends BaseAdapter {
    private Context mContext;

    public PreferenceAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return PreferenceConfig.getCount();
    }

    @Override
    public Object getItem(int position) {
        return PreferenceConfig.get(position);
    }

    @Override
    public long getItemId(int position) {
        return PreferenceConfig.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return PreferenceConfig.get(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return PreferenceConfig.get(position).getType() != PreferenceConfig.HEADER;
    }

    @Override
    public int getViewTypeCount() {
        return PreferenceConfig.getTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        View view;

        if (convertView != null && convertView.getTag() instanceof Integer
                && type == (Integer) convertView.getTag()) {
            view = convertView;
        } else {
            view = PreferenceConfig.getView(mContext, position);
        }

        //PreferenceConfig.get(position).setEnable(view, PreferenceConfig.isEnable(position));
        PreferenceConfig.get(position).bindView(view);
        return view;
    }
}
