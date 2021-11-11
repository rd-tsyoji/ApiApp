package jp.techacademy.takafumi.shouji.apiapp

import android.content.Context
import androidx.appcompat.app.AlertDialog

class DialogUtil {

   open fun showConfirmDeleteFavoriteDialog(
       id: String,
       showContext: Context,
       positiveCallback: Function<R>
   ) {
        AlertDialog.Builder(showContext)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveCallback
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->}
            .create()
            .show()
    }
}