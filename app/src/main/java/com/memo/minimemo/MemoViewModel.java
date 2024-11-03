package com.memo.minimemo;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.db.MemoRepository;

import java.util.List;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private final LiveData<List<MemoData>> mAllMemo;


    public MemoViewModel(Application application){
        super(application);
        memoRepository = new MemoRepository(application);
        mAllMemo = memoRepository.getAll();
    }

    public LiveData<List<MemoData>> getAll(){ return  mAllMemo; }

    public void insert(MemoData memoData){ memoRepository.insert(memoData); }

    public LiveData<MemoData> getById(long id){ return memoRepository.getById(id); }
}
