package com.criptext.mail.bgworker

/**
 * Abstraction over the multithreading code that can take BackgroundWorker instances and run them
 * without blocking the UI thread and eventually posting the worker's result to the UI thread.
 * Created by gabriel on 9/20/17.
 */

interface WorkRunner {
    fun workInBackground(worker: BackgroundWorker<*>)
}
