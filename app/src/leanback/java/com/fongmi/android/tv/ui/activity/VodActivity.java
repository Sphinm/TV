package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewpager.widget.ViewPager;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.ActivityVodBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.ui.adapter.TypeAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.fragment.FolderFragment;
import com.fongmi.android.tv.ui.helper.PanStorageHelper;
import com.fongmi.android.tv.utils.KeyUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Task;

import com.google.gson.JsonParser;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Optional;

public class VodActivity extends BaseActivity implements TypeAdapter.OnClickListener {

    public static final String PAN_CONFIG_SITE_KEY = "配置";
    private static final String PAN_CONFIG_TYPE = "夸克网盘配置";

    private ActivityVodBinding mBinding;
    private TypeAdapter mAdapter;
    private View mOldView;
    private boolean mLoadingPanConfig;

    public static void start(Activity activity, Result result) {
        start(activity, VodConfig.get().getHome().getKey(), result);
    }

    public static void start(Activity activity, String key, Result result) {
        start(activity, key, result, 0);
    }

    public static void start(Activity activity, String key, Result result, int position) {
        if (result == null || result.getTypes().isEmpty()) return;
        Intent intent = new Intent(activity, VodActivity.class);
        intent.putExtra("key", key);
        intent.putExtra("result", result);
        intent.putExtra("position", position);
        activity.startActivity(intent);
    }

    private String getKey() {
        return getIntent().getStringExtra("key");
    }

    private Result getResult() {
        return getIntent().getParcelableExtra("result");
    }

    private int getPosition() {
        return getIntent().getIntExtra("position", 0);
    }

    private Class getType() {
        return mAdapter.get(mBinding.pager.getCurrentItem());
    }

    private FolderFragment getFragment() {
        return (FolderFragment) mBinding.pager.getAdapter().instantiateItem(mBinding.pager, mBinding.pager.getCurrentItem());
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityVodBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setRecyclerView();
        setTypes();
        setPager();
        selectInitialTab();
    }

    @Override
    protected void initEvent() {
        mBinding.panConfig.setOnClickListener(view -> openPanConfig());
        mBinding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.recycler.setSelectedPosition(position);
                mBinding.recycler.requestFocus();
            }
        });
        mBinding.recycler.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                onChildSelected(child);
            }
        });
    }

    private void setRecyclerView() {
        mBinding.recycler.requestFocus();
        mBinding.recycler.setHorizontalSpacing(ResUtil.dp2px(16));
        mBinding.recycler.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.recycler.setAdapter(mAdapter = new TypeAdapter(this));
    }

    private void setTypes() {
        mAdapter.addAll(getResult().getTypes());
    }

    private void setPager() {
        mBinding.pager.setAdapter(new PageAdapter(getSupportFragmentManager()));
    }

    private void selectInitialTab() {
        int position = getPosition();
        if (position <= 0 || position >= mAdapter.getItemCount()) return;
        App.post(() -> selectTab(position), 100);
    }

    private void selectTab(int position) {
        if (position < 0 || position >= mAdapter.getItemCount()) return;
        mBinding.recycler.setSelectedPosition(position);
        mBinding.pager.setCurrentItem(position);
    }

    private Site getPanConfigSite() {
        Site site = VodConfig.get().getSite(PAN_CONFIG_SITE_KEY);
        if (!TextUtils.isEmpty(site.getKey()) && !TextUtils.isEmpty(site.getApi())) return site;
        Site ref = VodConfig.get().getSite("夸克");
        if (TextUtils.isEmpty(ref.getKey())) ref = VodConfig.get().getHome();
        return Site.objectFrom(JsonParser.parseString("{\"key\":\"配置\",\"name\":\"配置中心\",\"type\":3,\"api\":\"csp_Config\"}"), ref.getJar());
    }

    private void openPanConfig() {
        PanStorageHelper.runWithStorage(this, this::loadPanConfig);
    }

    private void loadPanConfig() {
        if (mLoadingPanConfig) return;
        Site site = getPanConfigSite();
        if (TextUtils.isEmpty(site.getApi())) {
            Notify.show(R.string.vod_pan_config_missing);
            return;
        }
        if (PAN_CONFIG_SITE_KEY.equals(getKey())) {
            int index = findPanConfigIndex(getResult());
            if (index >= 0) {
                selectTab(index);
                return;
            }
        }
        mLoadingPanConfig = true;
        mBinding.panConfig.setEnabled(false);
        Notify.show(R.string.vod_pan_config_loading);
        Task.execute(() -> {
            try {
                Result result = SiteApi.homeContent(site);
                int index = findPanConfigIndex(result);
                App.post(() -> onPanConfigLoaded(site.getKey(), result, index));
            } catch (Exception e) {
                App.post(this::onPanConfigFailed);
            }
        });
    }

    private void onPanConfigLoaded(String key, Result result, int index) {
        mLoadingPanConfig = false;
        mBinding.panConfig.setEnabled(true);
        if (result.getTypes().isEmpty() || index < 0) {
            Notify.show(R.string.vod_pan_config_error);
            return;
        }
        if (key.equals(getKey())) {
            selectTab(index);
            return;
        }
        start(this, key, result, index);
    }

    private void onPanConfigFailed() {
        mLoadingPanConfig = false;
        mBinding.panConfig.setEnabled(true);
        Notify.show(R.string.vod_pan_config_error);
    }

    private static int findPanConfigIndex(Result result) {
        List<Class> types = result.getTypes();
        for (int i = 0; i < types.size(); i++) {
            String name = types.get(i).getTypeName();
            if (name.contains(PAN_CONFIG_TYPE) || "quark".equals(types.get(i).getTypeId())) return i;
        }
        return types.isEmpty() ? -1 : 0;
    }

    private void onChildSelected(@Nullable RecyclerView.ViewHolder child) {
        if (mOldView != null) mOldView.setSelected(false);
        if ((mOldView = child != null ? child.itemView : null) == null) return;
        mOldView.setSelected(true);
        App.post(mRunnable, 100);
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mBinding.pager.setCurrentItem(mBinding.recycler.getSelectedPosition());
        }
    };

    private boolean isFilterVisible() {
        return Optional.ofNullable(getType()).map(Class::getFilter).orElse(false);
    }

    private void updateFilter() {
        Optional.ofNullable(getType()).ifPresent(this::updateFilter);
    }

    private void updateFilter(Class item) {
        item.setFilter(!item.getFilter());
        getFragment().toggleFilter(item.getFilter());
        mAdapter.notifyItemRangeChanged(mAdapter.indexOf(item), 1);
    }

    public void closeFilter() {
        if (isFilterVisible()) updateFilter();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.CATEGORY) getFragment().onRefresh();
    }

    @Override
    public void onItemClick(Class item) {
        updateFilter(item);
    }

    @Override
    public void onRefresh(Class item) {
        getFragment().onRefresh();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyUtil.isMenuKey(event)) updateFilter();
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onBackInvoked() {
        if (isFilterVisible()) updateFilter();
        else if (getFragment().canBack()) getFragment().goBack();
        else super.onBackInvoked();
    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Class type = mAdapter.get(position);
            return FolderFragment.newInstance(getKey(), type);
        }

        @Override
        public int getCount() {
            return mAdapter.getItemCount();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }
    }
}
