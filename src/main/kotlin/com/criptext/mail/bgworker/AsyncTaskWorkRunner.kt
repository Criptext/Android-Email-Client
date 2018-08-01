package com.criptext.mail.bgworker

import android.os.AsyncTask

/**
 * Created by gabriel on 2/16/18.
 */
class AsyncTaskWorkRunner: WorkRunner {

    override fun workInBackground(worker: BackgroundWorker<*>) {
        val task = MyAsyncTask(worker)
        if (worker.canBeParallelized)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        else
            task.execute()
    }

    private class MyAsyncTask<U>(private val worker: BackgroundWorker<U>)
        : AsyncTask<Void, U, U>() {

        private val reporter = object: ProgressReporter<U> {
            override fun report(progressPercentage: U) {
                publishProgress(progressPercentage)
            }
        }

        override fun doInBackground(vararg params: Void?): U? = worker.work(reporter)

        override fun onPostExecute(result: U?) {
            if (result != null)
                worker.publishFn(result)
        }

        override fun onProgressUpdate(vararg values: U) {
            values.forEach { worker.publishFn(it) }
        }
    }
}