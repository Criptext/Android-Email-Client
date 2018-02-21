package com.email.bgworker

import android.util.Log

/**
 * Created by gabriel on 2/20/18.
 */

abstract class WorkHandler<in I: Any, O: Any> {
    private val state: MutableMap<Class<out I>, WorkState<O>> = HashMap()

    abstract val runner: WorkRunner
    var listener: ((O) -> Unit)? = null
        set (newListener) {
            if (newListener != null) {
                state.keys.forEach({ key ->
                    val workState = state[key]
                    if (workState is WorkState.Done)  {
                    newListener(workState.result)
                    state.remove(key)
                }
            })
          }
          field = newListener
        }

    abstract fun createWorkerFromParams(params: I, flushResults: (O) -> Unit): BackgroundWorker<*>

    fun submitRequest(params: I) {
        val  isAvailable = state[params.javaClass] == null
        Log.d("submitRequest", "params $params isAvailable $isAvailable")
        if (isAvailable) {
            val worker = createWorkerFromParams(params, { result ->
                state[params.javaClass] = WorkState.Done(result)
                this.listener = this.listener
            })
            runner.workInBackground(worker)
        }
    }


}