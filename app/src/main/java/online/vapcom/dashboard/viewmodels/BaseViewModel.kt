/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel() {

    private val viewModelJob = Job()        // общий job для всех корутин этой viewModel
    protected val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)   // контекст корутин

    /**
     * Завершает все корутины когда эта viewModel завершает работу
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}