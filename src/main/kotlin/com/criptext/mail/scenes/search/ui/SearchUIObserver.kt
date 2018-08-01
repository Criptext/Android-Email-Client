package com.criptext.mail.scenes.search.ui

interface SearchUIObserver{
    fun onInputTextChange(text: String)
    fun onSearchButtonClicked(text: String)
    fun onBackButtonClicked()
}