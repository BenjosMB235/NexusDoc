package com.example.nexusdoc.ui.archives.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SectionViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String sectionType;

    public SectionViewModelFactory(Application application, String sectionType) {
        this.application = application;
        this.sectionType = sectionType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SectionViewModel.class)) {
            return (T) new SectionViewModel(application, sectionType);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}