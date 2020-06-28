package com.facecool.utils

fun CharSequence.isLetterAndDigitsOnly(): Boolean{
    forEach {
        if (it.isLetter().not() and it.isDigit().not()) return false
    }
    return true
}

fun CharSequence.isEmail(): Boolean{
//    return "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex().matches(this)
    return android.util.Patterns.EMAIL_ADDRESS.toRegex().matches(this)
}