package com.example.jetnews.ui.article

import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.seconds
import com.example.jetnews.data._posts
import com.example.jetnews.model.Post
import com.example.jetnews.ui.ScreenState
import kotlinx.coroutines.Dispatchers

typealias ArticleState = ScreenState<Post>

interface ArticleAlgebra {
    fun getArticle(postId: String, cb: (ArticleState) -> Unit): IO<Unit>

    companion object {
        operator fun invoke() = object : ArticleAlgebra {
            // for example purposes only :)
            var hasFailed = false

            override fun getArticle(postId: String, cb: (ArticleState) -> Unit): IO<Unit> =
                IO.fx {
                    !effect { cb(ScreenState.Loading) }
                    continueOn(Dispatchers.IO)
                    val post = _posts.find { it.id == postId }
                    !sleep(2.seconds)
                    !effect(Dispatchers.Main) {
                        cb(
                            // Emulate brittle BE
                            if (!hasFailed || post == null) {
                                hasFailed = true
                                ScreenState.Error(Unit)
                            } else {
                                ScreenState.Content(post)
                            }
                        )
                    }
                }
        }
    }
}