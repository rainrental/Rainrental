package org.rainrental.rainrentalrfid.inputmanager.data.model

import org.rainrental.rainrentalrfid.result.InputError

sealed interface InputFlowState{
    data object Initialising: InputFlowState
    data object Waiting : InputFlowState
    data object Scanning : InputFlowState

    data class Error(val error: InputError) : InputFlowState
    data class Success(val data:String) : InputFlowState
}


