package codes.jakob.graphenotype.utility


fun <T> Iterable<T>.toArrayList(): ArrayList<T> = ArrayList(this.toList())
