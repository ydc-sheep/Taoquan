package com.csuft.taoquan.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.views.TbNestedScrollView;
import com.csuft.taoquan.R;
import com.csuft.taoquan.base.BaseFragment;
import com.csuft.taoquan.model.domain.Categories;
import com.csuft.taoquan.model.domain.HomePagerContent;
import com.csuft.taoquan.model.domain.IBaseInfo;
import com.csuft.taoquan.presenter.ICategoryPagerPresenter;
import com.csuft.taoquan.ui.adapter.LinearItemContentAdapter;
import com.csuft.taoquan.ui.adapter.LooperPagerAdapter;
import com.csuft.taoquan.ui.custom.AutoLoopViewPager;
import com.csuft.taoquan.utils.Constants;
import com.csuft.taoquan.utils.LogUtils;
import com.csuft.taoquan.utils.PresenterManager;
import com.csuft.taoquan.utils.SizeUtils;
import com.csuft.taoquan.utils.TicketUtil;
import com.csuft.taoquan.utils.ToastUtil;
import com.csuft.taoquan.view.ICategoryPagerCallback;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;

public class HomePagerFragment extends BaseFragment implements ICategoryPagerCallback, LinearItemContentAdapter.OnListItemClickListener, LooperPagerAdapter.OnLooperPageItemClickListener {

    private ICategoryPagerPresenter mPagerPresenter;
    private int mMaterialId;
    private LinearItemContentAdapter mContentAdapter;
    private LooperPagerAdapter mLooperPagerAdapter;

    public static HomePagerFragment newInstance(Categories.DataBean category) {
        HomePagerFragment homePagerFragment = new HomePagerFragment();
        //
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_HOME_PAGER_TITLE,category.getTitle());
        bundle.putInt(Constants.KEY_HOME_PAGER_MATERIAL_ID,category.getId());
        homePagerFragment.setArguments(bundle);
        return homePagerFragment;
    }

    @BindView(R.id.home_pager_content_list)
    public RecyclerView mContentList;

    @BindView(R.id.looper_pager)
    public AutoLoopViewPager looperPager;

    @BindView(R.id.home_pager_title)
    public TextView currentCategoryTitleTv;

    @BindView(R.id.looper_point_container)
    public LinearLayout looperPointContainer;

    @BindView(R.id.home_pager_parent)
    public LinearLayout homePagerParent;

    @BindView(R.id.home_pager_header_container)
    public LinearLayout homeHeaderContainer;

    @BindView(R.id.home_pager_nested_scroller)
    public TbNestedScrollView homePagerNestedView;

    @BindView(R.id.home_pager_refresh)
    public TwinklingRefreshLayout twinklingRefreshLayout;

    @Override
    protected int getRootViewResId() {
        return R.layout.fragment_home_pager;
    }

    @Override
    public void onResume() {
        super.onResume();
        //????????????????????????????????????
        looperPager.startLoop();
        LogUtils.d(this,"onResume...");
    }

    @Override
    public void onPause() {
        super.onPause();
        //????????????????????????
        looperPager.stopLoop();
        LogUtils.d(this,"onPause...");
    }

    @Override
    protected void initListener() {
        mContentAdapter.setOnListItemClickListener(this);
        mLooperPagerAdapter.setOnLooperPageItemClickListener(this);
        homePagerParent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(homeHeaderContainer == null) {
                    return;
                }
                int headerHeight = homeHeaderContainer.getMeasuredHeight();
                //                LogUtils.d(HomePagerFragment.this,"headerHeight -- > " + headerHeight);
                homePagerNestedView.setHeaderHeight(headerHeight);
                int measuredHeight = homePagerParent.getMeasuredHeight();
                //                LogUtils.d(HomePagerFragment.this,"measuredHeight... -- > " + measuredHeight);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mContentList.getLayoutParams();
                //                LogUtils.d(HomePagerFragment.this,"layoutParams.height -=== > " + layoutParams.height);
                layoutParams.height = measuredHeight;
                mContentList.setLayoutParams(layoutParams);
                if(measuredHeight != 0) {
                    homePagerParent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        currentCategoryTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int measuredHeight = mContentList.getMeasuredHeight();
                //                LogUtils.d(HomePagerFragment.this,"measuredHeight  == > " + measuredHeight);
            }
        });
        looperPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(mLooperPagerAdapter.getDataSize() == 0) {
                    return;
                }
                int targetPosition = position % mLooperPagerAdapter.getDataSize();
                //???????????????
                updateLooperIndicator(targetPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        twinklingRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                LogUtils.d(HomePagerFragment.this,"?????????Loader more...");
                //????????????????????????
                if(mPagerPresenter != null) {
                    mPagerPresenter.loaderMore(mMaterialId);
                }
            }
        });
    }

    /**
     * ???????????????
     *
     * @param targetPosition
     */
    private void updateLooperIndicator(int targetPosition) {
        for(int i = 0; i < looperPointContainer.getChildCount(); i++) {
            View point = looperPointContainer.getChildAt(i);
            if(i == targetPosition) {
                point.setBackgroundResource(R.drawable.shape_indicator_point_selected);
            } else {
                point.setBackgroundResource(R.drawable.shape_indicator_point_normal);
            }
        }
    }

    @Override
    protected void initView(View rootView) {
        //?????????????????????
        mContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mContentList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
                outRect.top = SizeUtils.dip2px(getContext(),1.5f);
                outRect.bottom = SizeUtils.dip2px(getContext(),1.5f);
            }
        });
        //???????????????
        mContentAdapter = new LinearItemContentAdapter();
        //???????????????
        mContentList.setAdapter(mContentAdapter);
        //?????????????????????
        mLooperPagerAdapter = new LooperPagerAdapter();
        //???????????????
        looperPager.setAdapter(mLooperPagerAdapter);
        looperPager.setDuration(5000);
        //??????Refresh????????????
        twinklingRefreshLayout.setEnableRefresh(false);
        twinklingRefreshLayout.setEnableLoadmore(true);
        //   twinklingRefreshLayout.setBottomView();
    }

    @Override
    protected void initPresenter() {
        mPagerPresenter = PresenterManager.instance().getCategoryPagePresenter();
        mPagerPresenter.registerViewCallback(this);
    }

    @Override
    protected void loadData() {
        Bundle arguments = getArguments();
        String title = arguments.getString(Constants.KEY_HOME_PAGER_TITLE);
        mMaterialId = arguments.getInt(Constants.KEY_HOME_PAGER_MATERIAL_ID);
        LogUtils.d(this,"title -- > " + title);
        LogUtils.d(this,"materialId -- > " + mMaterialId);
        if(mPagerPresenter != null) {
            mPagerPresenter.getContentByCategoryId(mMaterialId);
        }
        if(currentCategoryTitleTv != null) {
            currentCategoryTitleTv.setText(title);
        }
    }

    @Override
    public void onContentLoaded(List<HomePagerContent.DataBean> contents) {
        //????????????????????????
        mContentAdapter.setData(contents);
        setUpState(State.SUCCESS);
    }

    @Override
    public int getCategoryId() {
        return mMaterialId;
    }

    @Override
    public void onLoading() {
        setUpState(State.LOADING);
    }

    @Override
    public void onError() {
        //????????????
        setUpState(State.ERROR);
    }

    @Override
    public void onEmpty() {
        setUpState(State.EMPTY);
    }

    @Override
    public void onLoaderMoreError() {
        ToastUtil.showToast("??????????????????????????????");
        if(twinklingRefreshLayout != null) {
            twinklingRefreshLayout.finishLoadmore();
        }
    }

    @Override
    public void onLoaderMoreEmpty() {
        ToastUtil.showToast("??????????????????");
        if(twinklingRefreshLayout != null) {
            twinklingRefreshLayout.finishLoadmore();
        }
    }

    @Override
    public void onLoaderMoreLoaded(List<HomePagerContent.DataBean> contents) {
        //?????????????????????????????????
        mContentAdapter.addData(contents);
        if(twinklingRefreshLayout != null) {
            twinklingRefreshLayout.finishLoadmore();
        }
        ToastUtil.showToast("?????????" + contents.size() + "?????????");
    }

    @Override
    public void onLooperListLoaded(List<HomePagerContent.DataBean> contents) {
        LogUtils.d(this,"looper size - - > " + contents.size());
        mLooperPagerAdapter.setData(contents);
        //?????????%?????????size????????????0???????????????????????????????????????
        //????????????
        int dx = (Integer.MAX_VALUE / 2) % contents.size();
        int targetCenterPosition = (Integer.MAX_VALUE / 2) - dx;
        //??????????????????
        looperPager.setCurrentItem(targetCenterPosition);
        //LogUtils.d(this," url  -- >" + contents.get(0).getPict_url());
        looperPointContainer.removeAllViews();
        //?????????
        for(int i = 0; i < contents.size(); i++) {
            View point = new View(getContext());
            int size = SizeUtils.dip2px(getContext(),8);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size,size);
            layoutParams.leftMargin = SizeUtils.dip2px(getContext(),5);
            layoutParams.rightMargin = SizeUtils.dip2px(getContext(),5);
            point.setLayoutParams(layoutParams);
            if(i == 0) {
                point.setBackgroundResource(R.drawable.shape_indicator_point_selected);
            } else {
                point.setBackgroundResource(R.drawable.shape_indicator_point_normal);
            }
            looperPointContainer.addView(point);
        }
    }

    @Override
    protected void release() {
        if(mPagerPresenter != null) {
            mPagerPresenter.unregisterViewCallback(this);
        }
    }

    @Override
    public void onItemClick(IBaseInfo item) {
        //????????????????????????
        LogUtils.d(this,"list item click --- > " + item.getTitle());
        handleItemClick(item);
    }

    private void handleItemClick(IBaseInfo item) {
        TicketUtil.toTicketPage(getContext(),item);
    }

    @Override
    public void onLooperItemClick(IBaseInfo item) {
        //???????????????????????????
        LogUtils.d(this,"looper item click --- > " + item.getTitle());
        handleItemClick(item);
    }
}
