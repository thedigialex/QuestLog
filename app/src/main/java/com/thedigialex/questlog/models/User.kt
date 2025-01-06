package com.thedigialex.questlog.models

data class User(
    var id: Int = 0,
    var username: String,
    var level: Int = 1,
    var exp: Int = 0,
    var coins: Int = 0,
    var equippedAvatarId: Int = 0,
    var equippedBackgroundId: Int = 0,
    var equippedTitleId: Int = 0,
    var equippedClassId: Int = 0,

    var avatarObtained: List<Item> = emptyList(),
    var backgroundObtained: List<Item> = emptyList(),
    var titleObtained: List<Item> = emptyList(),
    var classObtained: List<Item> = emptyList(),
    val isNew: Boolean = false
)
