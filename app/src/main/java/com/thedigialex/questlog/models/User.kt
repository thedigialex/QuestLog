package com.thedigialex.questlog.models

data class User(
    var id: Int = 0,
    var username: String,
    val level: Int = 1,
    val exp: Int = 0,
    val coins: Int = 0,
    val equippedAvatarId: Int = 0,
    val equippedBackgroundId: Int = 0,
    val equippedTitleId: Int = 0,
    val equippedClassId: Int = 0,

    val avatarIds: List<Int> = emptyList(),
    val backgroundIds: List<Int> = emptyList(),
    val titleIds: List<Int> = emptyList(),
    val classIds: List<Int> = emptyList(),
    val isNew: Boolean = false
)
