package com.csuft.taoquan.ui.fragment;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csuft.taoquan.R;
import com.csuft.taoquan.base.BaseFragment;
import com.csuft.taoquan.model.domain.IBaseInfo;
import com.csuft.taoquan.model.domain.SelectedContent;
import com.csuft.taoquan.model.domain.SelectedPageCategory;
import com.csuft.taoquan.presenter.ISelectedPagePresenter;
import com.csuft.taoquan.ui.adapter.SelectedPageContentAdapter;
import com.csuft.taoquan.ui.adapter.SelectedPageLeftAdapter;
import com.csuft.taoquan.utils.LogUtils;
import com.csuft.taoquan.utils.PresenterManager;
import com.csuft.taoquan.utils.SizeUtils;
import com.csuft.taoquan.utils.TicketUtil;
import com.csuft.taoquan.view.ISelectedPageCallback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class SelectedFragment extends BaseFragment
        implements ISelectedPageCallback,
        SelectedPageLeftAdapter.OnLeftItemClickListener,
        SelectedPageContentAdapter.OnSelectedPageContentItemClickListener {

    @BindView(R.id.left_category_list)
    public RecyclerView leftCategoryList;


    @BindView(R.id.right_content_list)
    public RecyclerView rightContentList;


    @BindView(R.id.fragment_bar_title_tv)
    public TextView barTitleTv;

    @Override
    protected View loadRootView(LayoutInflater inflater,ViewGroup container) {
        return inflater.inflate(R.layout.fragment_with_bar_layout,container,false);
    }


    private ISelectedPagePresenter mSelectedPagePresenter;
    private SelectedPageLeftAdapter mLeftAdapter;
    private SelectedPageContentAdapter mRightAdapter;

    @Override
    protected void initPresenter() {
        super.initPresenter();
        mSelectedPagePresenter = PresenterManager.instance().getSelectedPagePresenter();
        mSelectedPagePresenter.registerViewCallback(this);
        mSelectedPagePresenter.getCategories();
    }


    @Override
    protected void onRetryClick() {
        //??????
        if(mSelectedPagePresenter != null) {
            mSelectedPagePresenter.reloadContent();
        }
    }

    @Override
    protected void release() {
        super.release();
        if(mSelectedPagePresenter != null) {
            mSelectedPagePresenter.unregisterViewCallback(this);
        }
    }

    @Override
    protected int getRootViewResId() {
        return R.layout.fragment_selected;
    }

    @Override
    protected void initView(View rootView) {
        setUpState(State.SUCCESS);
        leftCategoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        mLeftAdapter = new SelectedPageLeftAdapter();
        leftCategoryList.setAdapter(mLeftAdapter);

        rightContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRightAdapter = new SelectedPageContentAdapter();
        rightContentList.setAdapter(mRightAdapter);
        rightContentList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
                int topAndBottom = SizeUtils.dip2px(getContext(),4);
                int leftAndRight = SizeUtils.dip2px(getContext(),6);
                outRect.left = leftAndRight;
                outRect.right = leftAndRight;
                outRect.top = topAndBottom;
                outRect.bottom = topAndBottom;
            }
        });
        barTitleTv.setText(getResources().getText(R.string.text_selected_title));
    }

    @Override
    protected void initListener() {
        super.initListener();
        mLeftAdapter.setOnLeftItemClickListener(this);
        mRightAdapter.setOnSelectedPageContentItemClickListener(this);
    }

    @Override
    public void onCategoriesLoaded(SelectedPageCategory categories) {
        setUpState(State.SUCCESS);
        mLeftAdapter.setData(categories);
        //????????????
        // LogUtils.d(this,"onCategoriesLoaded -- > " + categories);
        //??????????????????????????????????????????????????????
        //List<SelectedPageCategory.DataBean> data = categories.getData();
        //mSelectedPagePresenter.getContentByCategory(data.get(0));
    }

    @Override
    public void onContentLoaded(SelectedContent content) {
        mRightAdapter.setData(content);
        rightContentList.scrollToPosition(0);
    }

    @Override
    public void onError() {
        setUpState(State.ERROR);
    }

    @Override
    public void onLoading() {
        setUpState(State.LOADING);
    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onLeftItemClick(SelectedPageCategory.DataBean item) {
        //????????????????????????
        mSelectedPagePresenter.getContentByCategory(item);
        LogUtils.d(this,"current selected item -- > " + item.getFavorites_title());
    }

    @Override
    public void onContentItemClick(IBaseInfo item) {
        TicketUtil.toTicketPage(getContext(),item);
    }
}
