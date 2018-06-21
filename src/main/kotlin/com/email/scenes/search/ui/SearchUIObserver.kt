package com.email.scenes.search.ui

interface SearchUIObserver{
    fun onInputTextChange(text: String)
    fun onSearchButtonClicked(text: String)
    fun onBackButtonClicked()
}