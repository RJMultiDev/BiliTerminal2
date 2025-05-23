package com.huanli233.biliterminal2.activity.search;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.adapter.SearchHistoryAdapter;
import com.huanli233.biliterminal2.helper.TutorialHelper;
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager;
import com.huanli233.biliterminal2.util.view.AsyncLayoutInflaterX;
import com.huanli233.biliterminal2.util.JsonUtil;
import com.huanli233.biliterminal2.util.LinkUrlUtil;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import kotlin.Unit;

public class SearchActivity extends InstanceActivity {
    private String lastKeyword = "≠~`";
    private RecyclerView historyRecyclerview;
    SearchHistoryAdapter searchHistoryAdapter;
    ViewPager2 viewPager;
    EditText keywordInput;
    private ConstraintLayout searchBar;
    private boolean searchBarVisible = true;
    private boolean refreshing = false;
    private long animate_last;
    Handler handler;
    ArrayList<String> searchHistory;

    boolean tutorial_show;
    String classname;

    String[] specialList = {"自杀", "自尽", "自残", "抑郁", "双相"};

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        classname = getClass().getSimpleName();
        tutorial_show = Preferences.getBoolean("tutorial_pager_" + classname, true);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_search, null, (layoutView, id, parent) -> {
            setContentView(layoutView);
            setMenuClick();

            TutorialHelper.showTutorialList(this, R.array.tutorial_search, 4);

            handler = new Handler();

            viewPager = findViewById(R.id.viewPager);

            View searchBtn = findViewById(R.id.search);
            keywordInput = findViewById(R.id.keywordInput);
            searchBar = findViewById(R.id.searchbar);
            historyRecyclerview = findViewById(R.id.history_recyclerview);
            viewPager.setOffscreenPageLimit(4);
            keywordInput.setOnFocusChangeListener((view, b) -> historyRecyclerview.setVisibility(b ? View.VISIBLE : View.GONE));
            historyRecyclerview.setVisibility(View.VISIBLE);
            FragmentStateAdapter vpfAdapter = new FragmentStateAdapter(this) {
                @Override
                public int getItemCount() {
                    return 4;
                }

                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    if (position == 0) return SearchVideoFragment.newInstance();
                    if (position == 1) return SearchArticleFragment.newInstance();
                    if (position == 2) return SearchUserFragment.newInstance();
                    if (position == 3) return SearchLiveFragment.newInstance();
                    throw new IllegalArgumentException("return value of getItemCount() method maybe not associate with argument position");
                }
            };
            viewPager.setAdapter(vpfAdapter);

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position != 0) {
                        onScrolled(256);  //让搜索框隐藏
                        if (tutorial_show) {
                            tutorial_show = false;
                            findViewById(R.id.text_tutorial_pager).setVisibility(View.GONE);
                            Preferences.putBoolean("tutorial_pager_" + classname, false);
                        }
                    }

                    Fragment fragmentCurr = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                    if (fragmentCurr != null) {
                        ((SearchFragment) fragmentCurr).refresh();  //在fragment里已做判断
                    }
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            });

            searchBtn.setOnClickListener(view -> searchKeyword(keywordInput.getText().toString()));
            searchBtn.setOnLongClickListener(this::jumpToTargetId);
            keywordInput.setOnEditorActionListener((textView, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
                    searchKeyword(keywordInput.getText().toString());
                }
                return false;
            });

            try {
                searchHistory = JsonUtil.jsonToArrayList(new JSONArray(Preferences.getString(Preferences.SEARCH_HISTORY, "[]")), false);
            } catch (JSONException e) {
                runOnUiThread(() -> MsgUtil.error(e));
                searchHistory = new ArrayList<>();
            }
            searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistory);
            searchHistoryAdapter.setOnClickListener(position -> keywordInput.setText(searchHistory.get(position)));
            searchHistoryAdapter.setOnLongClickListener(position -> {
                MsgUtil.showMsg("删除成功");
                searchHistory.remove(position);
                searchHistoryAdapter.notifyItemRemoved(position);
                searchHistoryAdapter.notifyItemRangeChanged(position, searchHistory.size() - position);
                Preferences.putString(Preferences.SEARCH_HISTORY, new JSONArray(searchHistory).toString());
            });
            historyRecyclerview.setLayoutManager(new CustomLinearManager(this));
            historyRecyclerview.setAdapter(searchHistoryAdapter);


            if (getIntent().getStringExtra("keyword") != null) {
                findViewById(R.id.top_bar).setOnClickListener(view1 -> finish());
                keywordInput.setText(getIntent().getStringExtra("keyword"));
                MsgUtil.showMsg("可点击标题栏返回详情页");
            }

            return Unit.INSTANCE;
        });
    }

    public boolean jumpToTargetId(View view) {
        String text = keywordInput.getText().toString();
        LinkUrlUtil.handleId(this, text);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void searchKeyword(String str) {
        for (String s : specialList) {
            if (str.contains(s)) {
                MsgUtil.showText("特殊彩蛋", getString(R.string.egg_warmwords_warmworld));
                break;
            }
        }

        if (!refreshing) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View curFocus;
            if ((curFocus = getCurrentFocus()) != null) {
                manager.hideSoftInputFromWindow(curFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            if (str.isEmpty()) {
                runOnUiThread(() -> MsgUtil.showMsg("还没输入内容喵~"));
            } else if (Objects.equals(lastKeyword, str)) {
                runOnUiThread(() -> {
                    keywordInput.clearFocus();
                    historyRecyclerview.setVisibility(View.GONE);
                });
            } else {
                refreshing = true;
                lastKeyword = str;

                //搜索记录
                runOnUiThread(() -> {
                    historyRecyclerview.setVisibility(View.GONE);
                    keywordInput.clearFocus();
                });

                if (!searchHistory.contains(str)) {
                    try {
                        searchHistory.add(0, str);
                        Preferences.putString(Preferences.SEARCH_HISTORY, new JSONArray(searchHistory).toString());
                        runOnUiThread(() -> {
                            searchHistoryAdapter.notifyItemInserted(0);
                            searchHistoryAdapter.notifyItemRangeChanged(0, searchHistory.size());
                            historyRecyclerview.scrollToPosition(0);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.error(e));
                    }
                } else {
                    try {
                        int pos = searchHistory.indexOf(str);
                        searchHistory.remove(str);
                        searchHistory.add(0, str);
                        Preferences.putString(Preferences.SEARCH_HISTORY, new JSONArray(searchHistory).toString());
                        runOnUiThread(() -> {
                            searchHistoryAdapter.notifyItemMoved(pos, 0);
                            searchHistoryAdapter.notifyItemRangeChanged(0, searchHistory.size());
                            historyRecyclerview.scrollToPosition(0);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.error(e));
                    }
                }

                try {
                    for (int i = 0; i < 4; i++) {
                        Fragment fragmentById = getSupportFragmentManager().findFragmentByTag("f" + i);
                        if (fragmentById != null)
                            ((SearchFragment) fragmentById).update(str);
                    }
                    Fragment fragmentCurr = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                    if (fragmentCurr != null) {
                        ((SearchFragment) fragmentCurr).refresh();
                    }
                } catch (Exception e) {
                    report(e);
                }
                refreshing = false;
            }
        }
    }

    public void onScrolled(int dy) {
        float height = searchBar.getHeight() + Utils.dp2px(2f);

        if (System.currentTimeMillis() - animate_last > 200) {
            if (dy > 0 && searchBarVisible) {
                animate_last = System.currentTimeMillis();
                this.searchBarVisible = false;
                @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(searchBar, "translationY", 0, -height);
                animator.start();
                handler.postDelayed(() -> searchBar.setVisibility(View.GONE), 200);
            }
            if (dy < -1 && !searchBarVisible) {
                animate_last = System.currentTimeMillis();
                this.searchBarVisible = true;
                searchBar.setVisibility(View.VISIBLE);
                @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(searchBar, "translationY", -height, 0);
                animator.start();
            }
        }
    }

}