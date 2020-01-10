package com.example.jetnews.ui.home

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.seconds
import com.example.jetnews.data._posts
import com.example.jetnews.model.Post
import com.example.jetnews.ui.ScreenState
import kotlinx.coroutines.Dispatchers

typealias PostsState = ScreenState<List<Post>>

interface HomeAlgebra {
    fun getPosts(cb: (PostsState) -> Unit): IO<Unit>

    companion object {
        operator fun invoke() = object : HomeAlgebra {
            // for example purposes only :)
            var hasFailed = false

            override fun getPosts(cb: (PostsState) -> Unit): IO<Unit> = IO.fx {
                !effect { cb(ScreenState.Loading) }
                continueOn(Dispatchers.IO)
                val posts = _posts
                !sleep(2.seconds)
                !effect(Dispatchers.Main) {
                    cb(
                        // Emulate brittle BE
                        if (!hasFailed) {
                            hasFailed = true
                            ScreenState.Error(Unit)
                        } else {
                            ScreenState.Content(posts)
                        }
                    )
                }
            }
        }
    }
}


// TODO gotcha#2
// val asd by lazy {
//     IO { }.effectMap { }
// }
