package jp.techacademy.takafumi.shouji.apiapp

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    // お気に入り削除確認ダイアログ
    fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    fun addFavorite(favoriteShop: FavoriteShop) {
        FavoriteShop.insert(favoriteShop)
        Snackbar.make(findViewById(android.R.id.content), "お気に入りに追加しました", Snackbar.LENGTH_LONG)
            .show()
    }

    open fun deleteFavorite(id: String) {
        FavoriteShop.delete(id)
    }
}