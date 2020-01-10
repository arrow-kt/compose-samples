/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.ui.article

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.*
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.seconds
import com.example.jetnews.R
import com.example.jetnews.data._posts
import com.example.jetnews.model.Post
import com.example.jetnews.ui.Screen
import com.example.jetnews.ui.VectorImageButton
import com.example.jetnews.ui.home.BookmarkButton
import com.example.jetnews.ui.home.isFavorite
import com.example.jetnews.ui.home.toggleBookmark
import com.example.jetnews.ui.navigateTo
import kotlinx.coroutines.Dispatchers

interface ArticleAlgebra {
    fun getArticle(postId: String, cb: (ArticleViewState.GetArticle) -> Unit): IO<Unit>

    companion object {
        operator fun invoke() = object : ArticleAlgebra {
            override fun getArticle(postId: String, cb: (ArticleViewState.GetArticle) -> Unit): IO<Unit> = IO.fx {
                continueOn(Dispatchers.IO)
                val post = posts.find { it.id == postId }
                val articleViewState = post?.let { ArticleViewState.GetArticle.success(post) }
                        ?: ArticleViewState.GetArticle.failure(Throwable("Couldn't find the expected post"))
                !sleep(1.seconds)
                continueOn(Dispatchers.Main)
                !effect { cb(articleViewState) }
            }
        }
    }
}

enum class TaskStatus {
    SUCCESS, FAILURE, LOADING
}

sealed class ArticleViewState {
    data class GetArticle(
            val status: TaskStatus = TaskStatus.LOADING,
            val post: Post? = null,
            val error: Throwable? = null
    ) {
        companion object {
            internal fun loading() = GetArticle(TaskStatus.LOADING)
            internal fun success(post: Post) = GetArticle(TaskStatus.SUCCESS, post)
            internal fun failure(error: Throwable?) = GetArticle(TaskStatus.FAILURE, error = error)
        }
    }
}

@Composable
fun ArticleScreen(postId: String) {
    // getting the post from our list of posts by Id

    //val post = posts.find { it.id == postId } ?: return
    val algebra = +memo { ArticleAlgebra() }
    val (articleViewState, articleViewStateCb) = +state { ArticleViewState.GetArticle.loading() }

    +onActive {
        val d = algebra.getArticle(postId, articleViewStateCb).unsafeRunAsyncCancellable { }
        onDispose(d)
    }
    ArticlePost(articleViewState)
}

@Composable
fun ArticlePost(postViewState: ArticleViewState.GetArticle) {
    var showDialog by +state { false }
    if (showDialog) {
        FunctionalityNotAvailablePopup {
            showDialog = false
        }
    }

    Column {
        var topAppBarTitle = ""
        TopAppBar(
                title = {
                    Text(
                            text = topAppBarTitle,
                            style = (+MaterialTheme.typography()).subtitle2
                    )
                },
                navigationIcon = {
                    VectorImageButton(R.drawable.ic_back) {
                        navigateTo(Screen.Home)
                    }
                }
        )
        when(postViewState.status){
            TaskStatus.SUCCESS -> {
                topAppBarTitle = "Published in: ${postViewState.post!!.publication?.name}"
                PostContent(modifier = Flexible(1f), post = postViewState.post)
                BottomBar(postViewState.post) { showDialog = true }
            }
            TaskStatus.FAILURE -> TODO()
            TaskStatus.LOADING -> TODO()
        }
    }
}

@Composable
private fun BottomBar(post: Post, onUnimplementedAction: () -> Unit) {
    val context = +ambient(ContextAmbient)
    Surface(elevation = 2.dp) {
        Container(modifier = Height(56.dp) wraps Expanded) {
            Row {
                BottomBarAction(R.drawable.ic_favorite) {
                    onUnimplementedAction()
                }
                BookmarkButton(
                        isBookmarked = isFavorite(postId = post.id),
                        onBookmark = { toggleBookmark(postId = post.id) }
                )
                BottomBarAction(R.drawable.ic_share) {
                    sharePost(post, context)
                }
                Container(modifier = Flexible(1f)) { } // TODO: Any element works
                BottomBarAction(R.drawable.ic_text_settings) {
                    onUnimplementedAction()
                }
            }
        }
    }
}

@Composable
private fun BottomBarAction(
        @DrawableRes id: Int,
        onClick: () -> Unit
) {
    Ripple(
            bounded = false,
            radius = 24.dp
    ) {
        Clickable(onClick = onClick) {
            Container(modifier = Spacing(12.dp) wraps Size(24.dp, 24.dp)) {
                DrawVector(+vectorResource(id))
            }
        }
    }
}

@Composable
private fun FunctionalityNotAvailablePopup(onDismiss: () -> Unit) {
    AlertDialog(
            onCloseRequest = onDismiss,
            text = {
                Text(
                        text = "Functionality not available \uD83D\uDE48",
                        style = (+MaterialTheme.typography()).body2
                )
            },
            confirmButton = {
                Button(
                        text = "CLOSE",
                        style = TextButtonStyle(),
                        onClick = onDismiss
                )
            }
    )
}

private fun sharePost(post: Post, context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, post.title)
        putExtra(Intent.EXTRA_TEXT, post.url)
    }
    context.startActivity(Intent.createChooser(intent, "Share post"))
}

sealed class MVIViewState {
    object Loading : MVIViewState()
    class Error(val reason: String) : MVIViewState()
    class Success(val result: String) : MVIViewState()
}

@Preview
@Composable
fun previewArticle() {
    ArticleScreen(posts[4].id)
}
