package com.example.jetnews.ui.home

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.seconds
import com.example.jetnews.data._posts
import com.example.jetnews.model.Post
import kotlinx.coroutines.Dispatchers

interface HomeAlgebra {
    fun getPosts(cb: (List<Post>) -> Unit): IO<Unit>

    companion object {
        operator fun invoke() = object : HomeAlgebra {
            override fun getPosts(cb: (List<Post>) -> Unit): IO<Unit> = IO.fx {
                continueOn(Dispatchers.IO)
                val posts = _posts
                !sleep(1.seconds)
                continueOn(Dispatchers.Main)
                !effect { cb(posts) }
            }
        }
    }
}

// TODO gotcha#2
// val asd by lazy {
//     IO { }.effectMap { }
// }
