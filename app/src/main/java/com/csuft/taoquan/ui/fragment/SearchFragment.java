package com.csuft.taoquan.ui.fragment;

import android.app.Activity;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.csuft.taoquan.presenter.impl.IBackFragment;
import com.csuft.taoquan.ui.activity.IMainActivity;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.csuft.taoquan.R;
import com.csuft.taoquan.base.BaseFragment;
import com.csuft.taoquan.model.domain.Histories;
import com.csuft.taoquan.model.domain.IBaseInfo;
import com.csuft.taoquan.model.domain.SearchRecommend;
import com.csuft.taoquan.model.domain.SearchResult;
import com.csuft.taoquan.presenter.ISearchPresenter;
import com.csuft.taoquan.ui.adapter.LinearItemContentAdapter;
import com.csuft.taoquan.ui.custom.TextFlowLayout;
import com.csuft.taoquan.utils.KeyboardUtil;
import com.csuft.taoquan.utils.LogUtils;
import com.csuft.taoquan.utils.PresenterManager;
import com.csuft.taoquan.utils.SizeUtils;
import com.csuft.taoquan.utils.TicketUtil;
import com.csuft.taoquan.utils.ToastUtil;
import com.csuft.taoquan.view.ISearchPageCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;


public class SearchFragment extends BaseFragment implements IBackFragment, ISearchPageCallback, TextFlowLayout.OnFlowTextItemClickListener {


    @BindView(R.id.search_history_view)
    public TextFlowLayout mHistoriesView;

    @BindView(R.id.search_recommend_view)
    public TextFlowLayout mRecommendView;

    @BindView(R.id.search_recommend_container)
    public View mRecommendContainer;

    @BindView(R.id.search_history_container)
    public View mHistoriesContainer;


    @BindView(R.id.search_history_delete)
    public View mHistoryDelete;


    @BindView(R.id.search_result_list)
    public RecyclerView mSearchList;

    @BindView(R.id.search_btn)
    public TextView mSearchBtn;

    @BindView(R.id.search_clean_btn)
    public ImageView mCleanInputBtn;

    @BindView(R.id.search_input_box)
    public EditText mSearchInputBox;

    @BindView(R.id.search_back_press)
    public ImageView mSearchBackBtn;

    @BindView(R.id.search_result_container)
    public TwinklingRefreshLayout mRefreshContainer;


    private ISearchPresenter mSearchPresenter;
    private LinearItemContentAdapter mSearchResultAdapter;



    @Override
    protected void initPresenter() {
        mSearchPresenter = PresenterManager.instance().getSearchPresenter();
        mSearchPresenter.registerViewCallback(this);
        //?????????????????????
        mSearchPresenter.getRecommendWords();
        //mSearchPresenter.doSearch("??????");
        mSearchPresenter.getHistories();
    }


    @Override
    protected void onRetryClick() {
        //??????????????????
        if(mSearchPresenter != null) {
            mSearchPresenter.research();
        }
    }

    @Override
    protected void release() {
        super.release();
        if(mSearchPresenter != null) {
            mSearchPresenter.unregisterViewCallback(this);
        }
    }

    @Override
    protected View loadRootView(LayoutInflater inflater,ViewGroup container) {
        return inflater.inflate(R.layout.fragment_search_layout,container,false);
    }

    @Override
    protected int getRootViewResId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void initListener() {
        mHistoriesView.setOnFlowTextItemClickListener(this);
        mRecommendView.setOnFlowTextItemClickListener(this);
        getActivity().onBackPressed();
        mSearchBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasInput(false)){
                    mSearchInputBox.setText("");
                    //????????????????????????
                    switch2HistoryPage();
                } else {
                    FragmentActivity activity = getActivity();
                    if(activity instanceof IMainActivity) {
                        ((IMainActivity) activity).backToHome();;
                    }
                }
            }
        });
        //????????????
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????????????????????
                //????????????????????????????????????

                if(hasInput(false)) {
                    //????????????
                    if(mSearchPresenter != null) {
                        //mSearchPresenter.doSearch(mSearchInputBox.getText().toString().trim());
                        toSearch(mSearchInputBox.getText().toString().trim());
                        KeyboardUtil.hide(getContext(),v);
                    }
                } else {
                    ToastUtil.showToast("??????????????????????????????...");
                    //????????????
                    KeyboardUtil.hide(getContext(),v);
                    mSearchBtn.setText("??????");
                }
            }
        });
        //???????????????????????????
        mCleanInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInputBox.setText("");
                //????????????????????????
                switch2HistoryPage();
            }
        });

        //??????????????????????????????
        mSearchInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {

            }

            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                //?????????????????????
                //LogUtils.d(SearchFragment.this,"input text === > " + s.toString().trim());
                //??????????????????0???????????????????????????
                //????????????????????????
                mCleanInputBtn.setVisibility(hasInput(true) ? View.VISIBLE : View.GONE);
                mSearchBtn.setText(hasInput(false) ? "??????" : "??????");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //
            }
        });
        mSearchInputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v,int actionId,KeyEvent event) {
                //LogUtils.d(SearchFragment.this,"actionId === > " + actionId);
                if(actionId == EditorInfo.IME_ACTION_SEARCH && mSearchPresenter != null) {
                    String keyword = v.getText().toString().trim();
                    if(TextUtils.isEmpty(keyword)) {
                        return false;
                    }
                    //?????????????????????????????????
                    LogUtils.d(SearchFragment.this," input text === > " + keyword);
                    //????????????
                    toSearch(keyword);
                    //mSearchPresenter.doSearch(keyword);
                }
                return false;
            }
        });
        mHistoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
                mSearchPresenter.delHistories();
            }
        });

        mRefreshContainer.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                //?????????????????????
                if(mSearchPresenter != null) {
                    mSearchPresenter.loaderMore();
                }
            }
        });

        mSearchResultAdapter.setOnListItemClickListener(new LinearItemContentAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(IBaseInfo item) {
                //??????????????????????????????
                TicketUtil.toTicketPage(getContext(),item);
            }
        });

    }

    /**
     * ??????????????????????????????
     */
    private void switch2HistoryPage() {
        if(mSearchPresenter != null) {
            mSearchPresenter.getHistories();
        }
        if(mRecommendView.getContentSize() != 0) {
            mRecommendContainer.setVisibility(View.VISIBLE);
        } else {
            mRecommendContainer.setVisibility(View.GONE);
        }
        //???????????????
        mRefreshContainer.setVisibility(View.GONE);
    }

    private boolean hasInput(boolean containSpace) {
        if(containSpace) {
            return mSearchInputBox.getText().toString().length() > 0;
        } else {
            return mSearchInputBox.getText().toString().trim().length() > 0;
        }
    }

    @Override
    protected void initView(View rootView) {
        //?????????????????????
        mSearchList.setLayoutManager(new LinearLayoutManager(getContext()));
        //???????????????
        mSearchResultAdapter = new LinearItemContentAdapter();
        mSearchList.setAdapter(mSearchResultAdapter);
        //??????????????????
        mRefreshContainer.setEnableLoadmore(true);
        mRefreshContainer.setEnableRefresh(false);
        mRefreshContainer.setEnableOverScroll(true);
        mSearchList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
                outRect.top = SizeUtils.dip2px(getContext(),1.5f);
                outRect.bottom = SizeUtils.dip2px(getContext(),1.5f);
                ;
            }
        });
    }

    @Override
    public void onHistoriesLoaded(Histories histories) {
        setUpState(State.SUCCESS);
        LogUtils.d(this,"histories -- > " + histories);
        if(histories == null || histories.getHistories().size() == 0) {
            mHistoriesContainer.setVisibility(View.GONE);
        } else {
            mHistoriesContainer.setVisibility(View.VISIBLE);
            mHistoriesView.setTextList(histories.getHistories());
        }
    }

    @Override
    public void onHistoriesDeleted() {
        //??????????????????
        if(mSearchPresenter != null) {
            mSearchPresenter.getHistories();
        }
    }

    @Override
    public void onSearchSuccess(SearchResult result) {
        setUpState(State.SUCCESS);
        //LogUtils.d(this," result -=- > " + result);
        //??????????????????????????????
        mRecommendContainer.setVisibility(View.GONE);
        mHistoriesContainer.setVisibility(View.GONE);
        //??????????????????
        mRefreshContainer.setVisibility(View.VISIBLE);
        //????????????
        try {
            mSearchResultAdapter.setData(result.getData()
                    .getTbk_dg_material_optional_response()
                    .getResult_list()
                    .getMap_data());
        } catch(Exception e) {
            e.printStackTrace();
            //???????????????????????????
            setUpState(State.EMPTY);
        }
    }

    @Override
    public void onMoreLoaded(SearchResult result) {
        mRefreshContainer.finishLoadmore();
        //????????????????????????
        //??????????????????????????????????????????
        List<SearchResult.DataBean.TbkDgMaterialOptionalResponseBean.ResultListBean.MapDataBean> moreData = result.getData().getTbk_dg_material_optional_response().getResult_list().getMap_data();
        mSearchResultAdapter.addData(moreData);
        //??????????????????????????????
        ToastUtil.showToast("????????????" + moreData.size() + "?????????");
    }

    @Override
    public void onMoreLoadedError() {
        mRefreshContainer.finishLoadmore();
        ToastUtil.showToast("??????????????????????????????");
    }

    @Override
    public void onMoreLoadedEmpty() {
        mRefreshContainer.finishLoadmore();
        ToastUtil.showToast("??????????????????");
    }

    @Override
    public void onRecommendWordsLoaded(List<SearchRecommend.DataBean> recommendWords) {
        setUpState(State.SUCCESS);
        LogUtils.d(this,"recommendWords size --- > " + recommendWords.size());
        List<String> recommendKeywords = new ArrayList<>();
        for(SearchRecommend.DataBean item : recommendWords) {
            recommendKeywords.add(item.getKeyword());
        }
        if(recommendWords == null || recommendWords.size() == 0) {
            mRecommendContainer.setVisibility(View.GONE);
        } else {
            mRecommendView.setTextList(recommendKeywords);
            mRecommendContainer.setVisibility(View.VISIBLE);
        }
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
        setUpState(State.EMPTY);
    }

    @Override
    public void onFlowItemClick(String text) {
        //????????????
        toSearch(text);
    }

    private void toSearch(String text) {
        if(mSearchPresenter != null) {
            mSearchList.scrollToPosition(0);
            mSearchInputBox.setText(text);
            mSearchInputBox.setFocusable(true);
            mSearchInputBox.requestFocus();
            //mSearchInputBox.setSelection(text.length());
            mSearchInputBox.setSelection(text.length(),text.length());
            mSearchPresenter.doSearch(text);
        }
    }

    @Override
    public boolean onBackPressed() {
//        if (i==1) {
//            //action not popBackStack
//            return true;
//        } else {
//            return false;
//        }
        mSearchBackBtn.callOnClick();
        return false;
    }
}
