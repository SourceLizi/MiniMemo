package com.memo.minimemo;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.material.appbar.MaterialToolbar;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.db.MemoRepository;

import java.util.List;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private final LiveData<List<MemoData>> mAllMemo;

    private FragmentContentBinding content_binding;
    private MemoData currEditing;

    public void setContent_binding(FragmentContentBinding content_binding) {
        this.content_binding = content_binding;
    }

    public FragmentContentBinding getContent_binding() {
        return content_binding;
    }

    public MemoData getCurrEditing() {
        synchronized(this){
            return currEditing;
        }
    }

    public void setCurrEditing(MemoData currEditing) {
        synchronized(this) {
            this.currEditing = currEditing;
        }
    }

    public MemoViewModel(Application application){
        super(application);
        memoRepository = new MemoRepository(application);
        mAllMemo = memoRepository.getAll();
    }

    public LiveData<List<MemoData>> getAll(){ return  mAllMemo; }

    public LiveData<MemoData> getById(long id){ return memoRepository.getById(id); }

    public void insert(MemoData memoData){ memoRepository.insert(memoData); }
    public void update(MemoData memoData){ memoRepository.update(memoData); }
    public void deleteById(long id){ memoRepository.deleteById(id); }

}
