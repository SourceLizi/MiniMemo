package com.memo.minimemo.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MemoRepository {
    private MemoDao mMemoDao;
    private LiveData<List<MemoData>> mAllMemos;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public MemoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mMemoDao = db.memoDao();
        mAllMemos = mMemoDao.selectAll();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<MemoData>> getAll() {
        return mAllMemos;
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<MemoData> getById(long id) {
        return mMemoDao.selectById(id);
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(MemoData m) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mMemoDao.insertAll(m);
        });
    }
}
