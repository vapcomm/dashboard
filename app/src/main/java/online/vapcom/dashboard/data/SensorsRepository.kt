/*
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.data

import androidx.lifecycle.LiveData

interface SensorsRepository {
    suspend fun start() : RepoReply
    fun getSpeed(): LiveData<Int>
    fun getRPM(): LiveData<Int>
}
