package com.antonelli.servipedia.utils

interface BottomSheetListener {
    fun closeBottomSheet()
    fun envioRating(newVote: String?, newRating: Double, delete: Boolean, positionVotes: Int)
}
