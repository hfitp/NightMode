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
package com.awaysoft.nightlymode;

import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.awaysoft.nightlymode.utils.Preference;
import com.awaysoft.nightlymode.utils.Utils;
import com.awaysoft.nightlymode.widget.BaseActivity;
import com.awaysoft.widget.component.ActionBar;

import java.util.ArrayList;
import java.util.List;

/**
 * AppSelectActivity.
 *
 * @author ruikye
 * @since 2014/7/27.
 */
public class AppSelectActivity extends BaseActivity {
    private List<AppItem> mInstalledApps;
    private AppSelectorAdapter mSelectorAdapter;
    private AsyncTask mLoadInstalledTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nightly_app_selector);

        ListView listView = (ListView) findViewById(R.id.nighlty_listview);
        ActionBar actionBar = (ActionBar) findViewById(R.id.nightly_actionbar);
        actionBar.setTitleClickLinstener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSelectorAdapter = new AppSelectorAdapter();

        View emptyView = findViewById(R.id.empty);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mSelectorAdapter);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(50);
        listView.setLayoutAnimation(new LayoutAnimationController(alphaAnimation));

        //for security
        Preference.INSTANCE.read(this);

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        mLoadInstalledTask = new AsyncTask<Void, Void, List<AppItem>>() {
            @Override
            protected List<AppItem> doInBackground(Void... params) {
                PackageManager packageManager = getPackageManager();
                List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_ACTIVITIES);

                List<AppItem> installed = new ArrayList<AppItem>();
                for (ApplicationInfo app : apps) {
                    //Only show contain CATEGORY_LAUNCHER
                    List list = Utils.INSTANCE.findActivitiesForPackage(AppSelectActivity.this, app.packageName);
                    if (list == null || list.isEmpty()) {
                        continue;
                    }

                    AppItem item = new AppItem();
                    item.icon = packageManager.getApplicationIcon(app);
                    item.name = packageManager.getApplicationLabel(app).toString();
                    item.pkgName = app.packageName;
                    installed.add(item);
                }

                return installed;
            }

            @Override
            protected void onPostExecute(List<AppItem> packageInfos) {
                mLoadInstalledTask = null;
                mInstalledApps = packageInfos;
                mSelectorAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private class AppItem {
        Drawable icon;
        String name;
        String pkgName;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Save configuration
        Preference.INSTANCE.save(AppSelectActivity.this);

        if (mLoadInstalledTask != null) {
            mLoadInstalledTask.cancel(true);
            mLoadInstalledTask = null;
        }
    }

    private class AppSelectorAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mInstalledApps == null ? 0 : mInstalledApps.size();
        }

        @Override
        public AppItem getItem(int position) {
            return mInstalledApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        @TargetApi(11)
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ItemHolder item;
            if (convertView == null) {
                convertView = View.inflate(AppSelectActivity.this, R.layout.nightly_item_icon, null);
                convertView.setBackgroundResource(R.drawable.list_item_drawable);
                Resources res = getResources();
                convertView.setPadding(res.getDimensionPixelSize(R.dimen.nightly_listitem_padding),
                        res.getDimensionPixelSize(R.dimen.default_padding),
                        res.getDimensionPixelSize(R.dimen.nightly_listitem_padding),
                        res.getDimensionPixelSize(R.dimen.default_padding));

                item = new ItemHolder();
                item.icon = (ImageView) convertView.findViewById(R.id.nighlty_item_icon);
                item.name = (TextView) convertView.findViewById(R.id.item_label);
                item.pkgName = (TextView) convertView.findViewById(R.id.item_explain);
                item.checkBox = (CheckBox) convertView.findViewById(R.id.nightly_checkbox);
                convertView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemHolder itemHolder = (ItemHolder) v.getTag();
                        AppItem item = mInstalledApps.get(itemHolder.index);

                        boolean enable = Preference.INSTANCE.inWhiteList(item.pkgName);
                        itemHolder.checkBox.setChecked(!enable);
                        Preference.INSTANCE.enableInWhiteList(item.pkgName, !enable);
                    }
                });
            } else {
                item = (ItemHolder) convertView.getTag();
            }

            AppItem pkg = getItem(position);
            boolean isSelf = TextUtils.equals(getPackageName(), pkg.pkgName);

            item.index = position;
            convertView.setTag(item);
            convertView.setEnabled(!isSelf);

            item.name.setText(pkg.name);
            item.pkgName.setText(pkg.pkgName);
            item.icon.setImageDrawable(pkg.icon);
            item.checkBox.setChecked(Preference.INSTANCE.inWhiteList(pkg.pkgName));
            item.checkBox.setEnabled(!isSelf);
            item.name.setEnabled(!isSelf);
            item.pkgName.setEnabled(!isSelf);

            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                item.icon.setAlpha(isSelf ? 0.2f : 1f);
            }

            return convertView;
        }
    }

    private static class ItemHolder {
        int index;
        ImageView icon;
        TextView name;
        TextView pkgName;
        CheckBox checkBox;
    }
}
