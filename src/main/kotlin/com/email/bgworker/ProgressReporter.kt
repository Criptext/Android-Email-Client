package com.email.bgworker

/**
 * Created by gabriel on 6/5/18.
 */
interface ProgressReporter<in T> {

    fun report(progressPercentage: T)

}