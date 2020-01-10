package com.example.jetnews

fun <A> List<A>.safeSublist(fromIndex: Int, toIndex: Int) =
    if (isNotEmpty()) subList(fromIndex, toIndex) else emptyList()
