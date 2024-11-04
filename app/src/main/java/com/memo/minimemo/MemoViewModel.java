package com.memo.minimemo;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.db.MemoRepository;

import java.util.List;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private final LiveData<List<MemoData>> mAllMemo;
    private LiveData<List<MemoData>> searchResult;
    private final MemoListAdapter adapter;

    private LifecycleOwner lifecycleOwner;
    private FragmentContentBinding content_binding;
    private MemoData currEditing;

    public MemoListAdapter getAdapter() {
        return adapter;
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }

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
        this.mAllMemo = memoRepository.getAll();
        this.adapter = new MemoListAdapter(new MemoListAdapter.MemoDataDiff());

    }

    public void setDefaultData(){
        if(this.adapter !=null && lifecycleOwner != null){
            if(searchResult != null && searchResult.hasObservers())
                searchResult.removeObservers(lifecycleOwner);
            if(mAllMemo.hasObservers())
                mAllMemo.removeObservers(lifecycleOwner);
            mAllMemo.observe(lifecycleOwner, this.adapter::submitList);
        }
    }

    public LiveData<List<MemoData>> getAll(){ return mAllMemo; }

    public void find(String s){
        if(lifecycleOwner != null) {
            if (searchResult != null && searchResult.hasObservers())
                searchResult.removeObservers(lifecycleOwner);
            if (mAllMemo.hasObservers())
                mAllMemo.removeObservers(lifecycleOwner);
            searchResult = memoRepository.searchString(s);
            if(this.adapter !=null){
                searchResult.observe(lifecycleOwner, this.adapter::submitList);
            }
        }
    }

    public LiveData<MemoData> getById(long id){ return memoRepository.getById(id); }

    public void insert(MemoData memoData){ memoRepository.insert(memoData); }
    public void update(MemoData memoData){ memoRepository.update(memoData); }
    public void deleteById(long id){ memoRepository.deleteById(id); }

}
