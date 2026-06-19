package com.crowdcast.boomvilmap.ui;

import android.os.Bundle;
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
        layoutEmptySearch = view.findViewById(R.id.layout_empty_search);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 초기 화면: 빈 값 전송으로 전체 목록 로드
        List<Spot> initialSpots = SpotRepository.searchSpots("");
        adapter = new SpotAdapter(initialSpots, spotId ->
                ((MainActivity) requireActivity()).showDetail(spotId)
        );
        recyclerView.setAdapter(adapter);
        updateResultUi(initialSpots.size());

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
                return true; // 이벤트 소비 (키보드 내려감)
            }
            return false;
        });

        // 'X' 클리어 버튼 누르면 입력창 초기화 및 전체목록 복귀
        buttonClearSearch.setOnClickListener(v -> {
            editSearch.setText("");
            performSearch(); // 빈 값으로 검색해서 전체 목록 복구
        });
    }

    // 실제 검색 실행 로직 분리
    private void performSearch() {
        String query = editSearch.getText().toString();
        List<Spot> resultSpots = SpotRepository.searchSpots(query);

        adapter.updateData(resultSpots);
        updateResultUi(resultSpots.size());
    }

    private void updateResultUi(int count) {
        if (textResultCount != null) {
            textResultCount.setText("검색 결과 " + count + "건");
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