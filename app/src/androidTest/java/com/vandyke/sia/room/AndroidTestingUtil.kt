package com.vandyke.sia.room

/** essentially compares lists for equality but ignores order */
infix fun <T> List<T>.shouldEqualIgnoreOrder(other: List<T>): List<T> {
    assert(this.size == other.size && this.sortedBy { it!!.hashCode() } == other.sortedBy { it!!.hashCode() })
    return this
}