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
import androidx.compose.Composable
import androidx.compose.ambient
import androidx.compose.memo
import androidx.compose.onActive
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.Expanded
import androidx.ui.layout.Height
import androidx.ui.layout.HeightSpacer
import androidx.ui.layout.Row
import androidx.ui.layout.Size
import androidx.ui.layout.Spacing
import androidx.ui.material.AlertDialog
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TextButtonStyle
import androidx.ui.material.TopAppBar
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.material.withOpacity
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import arrow.fx.typeclasses.Disposable
import com.example.jetnews.R
import com.example.jetnews.data._posts
import com.example.jetnews.model.Post
import com.example.jetnews.ui.Screen
import com.example.jetnews.ui.ScreenState
import com.example.jetnews.ui.VectorImageButton
import com.example.jetnews.ui.home.BookmarkButton
import com.example.jetnews.ui.home.isFavorite
import com.example.jetnews.ui.home.toggleBookmark
import com.example.jetnews.ui.navigateTo

@Composable
fun ArticleScreen(postId: String) {
    val algebra = +memo { ArticleAlgebra() }
    val (postState, postStateCb) = +state<ArticleState> { ScreenState.Loading }

    fun load(): Disposable = algebra.getArticle(postId, postStateCb).unsafeRunAsyncCancellable { }

    +onActive {
        onDispose(load())
    }

    PostStateLCE(postState) {
        //TODO how to dispose this?
        load()
    }
}

@Composable
fun PostStateLCE(articleState: ArticleState, retryClick: () -> Unit) {
    val (showDialog, showDialogCb) = +state { false }
    if (showDialog) {
        FunctionalityNotAvailablePopup {
            showDialogCb(false)
        }
    }

    Column {
        val (topAppBarTitle, topAppBarTitleCb) = +state { "" }
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
        when (articleState) {
            ScreenState.Loading -> PostLoading()
            is ScreenState.Content -> {
                topAppBarTitleCb("Published in: ${articleState.value.publication?.name}")
                PostContent(modifier = Flexible(1f), post = articleState.value)
                BottomBar(articleState.value) { showDialogCb(true) }
            }
            is ScreenState.Error -> PostError(retryClick)
        }
    }
}

@Composable
private fun PostLoading() {
    Text(
        modifier = Spacing(top = 16.dp, left = 16.dp, right = 16.dp),
        text = "Loading content...",
        style = ((+MaterialTheme.typography()).subtitle1).withOpacity(0.87f)
    )
}

@Composable
private fun PostError(retryClick: () -> Unit) {
    Column(modifier = Spacing(16.dp)) {
        Text(
            text = "There was an error loading the content, please try again.",
            style = ((+MaterialTheme.typography()).subtitle1).withOpacity(0.87f)
        )
        HeightSpacer(height = 16.dp)
        Button(
            text = "Retry", onClick = retryClick
        )
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

@Preview
@Composable
fun previewArticle() {
    ArticleScreen(_posts[4].id)
}
