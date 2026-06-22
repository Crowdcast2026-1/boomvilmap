package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crowdcast.boomvilmap.R;
import com.crowdcast.boomvilmap.adepter.SpotAdapter;
import com.crowdcast.boomvilmap.model.Spot;
import com.crowdcast.boomvilmap.repository.SpotRepository;

import java.util.List;

public class SearchFragment extends Fragment {

    private SpotAdapter adapter;
    private TextView textResultCount;
    private LinearLayout layoutEmptySearch;
    private EditText editSearch;
    private NestedScrollView scrollSearchResults;
    private TextView textSortOrder;
    private SpotRepository.SearchSortOrder sortOrder = SpotRepository.SearchSortOrder.HIGH_TO_LOW;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editSearch = view.findViewById(R.id.edit_search);
        ImageButton buttonClearSearch = view.findViewById(R.id.button_clear_search);
        textResultCount = view.findViewById(R.id.text_result_count);
        textSortOrder = view.findViewById(R.id.text_sort_order);
        layoutEmptySearch = view.findViewById(R.id.layout_empty_search);
        scrollSearchResults = view.findViewById(R.id.scroll_search_results);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);

        // 초기 화면: 빈 값 전송으로 전체 목록 로드
        List<Spot> initialSpots = SpotRepository.searchSpots("");
        adapter = new SpotAdapter(initialSpots, spotId ->
                ((MainActivity) requireActivity()).showDetail(spotId)
        );
        recyclerView.setAdapter(adapter);
        updateResultUi(initialSpots.size());
        loadSearchDataIfNeeded();
        updateSortLabel();

        ImageView imageSearchIcon = view.findViewById(R.id.image_search_icon);

        imageSearchIcon.setOnClickListener(v -> {
            performSearch(); // 기존에 만들어둔 검색 실행 메서드 호출

            // 클릭 시 키보드 내리기 (선택 사항)
            android.view.inputmethod.InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        });

        // 키보드에서 '돋보기' 또는 '엔터' 버튼을 눌렀을 때만 검색 수행
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                android.view.inputmethod.InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                return true; // 이벤트 소비 (키보드 내려감)
            }
            return false;
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        textSortOrder.setOnClickListener(v -> {
            sortOrder = sortOrder == SpotRepository.SearchSortOrder.HIGH_TO_LOW
                    ? SpotRepository.SearchSortOrder.LOW_TO_HIGH
                    : SpotRepository.SearchSortOrder.HIGH_TO_LOW;
            updateSortLabel();
            performSearch();
        });

        // 'X' 클리어 버튼 누르면 입력창 초기화 및 전체목록 복귀
        buttonClearSearch.setOnClickListener(v -> {
            editSearch.setText("");
        });
    }

    // 실제 검색 실행 로직 분리
    private void performSearch() {
        String query = editSearch.getText().toString();
        List<Spot> resultSpots = SpotRepository.searchSpots(query, sortOrder);

        adapter.updateData(resultSpots);
        updateResultUi(resultSpots.size());
        if (scrollSearchResults != null) {
            scrollSearchResults.post(() -> scrollSearchResults.scrollTo(0, 0));
        }
    }

    private void updateSortLabel() {
        if (textSortOrder == null) return;
        textSortOrder.setText(sortOrder == SpotRepository.SearchSortOrder.HIGH_TO_LOW
                ? "혼잡도 높은순"
                : "혼잡도 낮은순");
    }

    private void loadSearchDataIfNeeded() {
        if (!SpotRepository.getSpots().isEmpty()) return;

        if (textResultCount != null) {
            textResultCount.setText("검색 데이터 불러오는 중...");
        }
        if (layoutEmptySearch != null) {
            layoutEmptySearch.setVisibility(View.GONE);
        }
        if (scrollSearchResults != null) {
            scrollSearchResults.setVisibility(View.GONE);
        }

        SpotRepository.fetchRealTimeSpots(new SpotRepository.OnSpotsLoadedListener() {
            @Override
            public void onSuccess(List<Spot> spots) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> performSearch());
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (textResultCount != null) {
                        textResultCount.setText("검색 데이터 로드 실패");
                    }
                });
            }
        });
    }

    private void updateResultUi(int count) {
        if (textResultCount != null) {
            textResultCount.setText("검색 결과 " + count + "건");
        }

        if (scrollSearchResults != null) {
            scrollSearchResults.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        }

        if (layoutEmptySearch != null) {
            if (count == 0) {
                layoutEmptySearch.setVisibility(View.VISIBLE);
            } else {
                layoutEmptySearch.setVisibility(View.GONE);
            }
        }
    }
}
