package com.vandyke.sia.ui.renter.contracts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vandyke.sia.data.models.renter.ContractData
import com.vandyke.sia.data.repository.RenterRepository
import com.vandyke.sia.util.rx.*
import javax.inject.Inject

class ContractsViewModel
@Inject constructor(
        private val renterRepository: RenterRepository
): ViewModel() {

    val contracts = MutableLiveData<List<ContractData>>()

    val refreshing = MutableNonNullLiveData(false)

    val error = MutableSingleLiveEvent<Throwable>()

    init {
        renterRepository.contracts()
                .io()
                .main()
                .subscribe(contracts::setValue, error::setValue)
    }

    fun refresh() {
        renterRepository.updateContracts()
                .io()
                .main()
                .track(refreshing)
                .subscribe({}, error::setValue)
    }
}