package jp.techacademy.takafumi.shouji.apiapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import jp.techacademy.takafumi.shouji.apiapp.databinding.ActivityWebViewBinding

class WebViewActivity : BaseActivity() {

    companion object {
        private const val KEY_SHOP = "key_shop"
        fun start(activity: Activity, favoriteShop: FavoriteShop) {
            activity.startActivity(
                Intent(activity, WebViewActivity::class.java).putExtra(
                    KEY_SHOP,
                    favoriteShop
                )
            )
        }
    }

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var shopData: FavoriteShop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        shopData = intent.getSerializableExtra(KEY_SHOP) as FavoriteShop
        binding.webView.loadUrl(shopData.url)
        binding.favoriteButton.setOnClickListener {
            onClickFavoriteButton()
        }
        supportActionBar?.setDisplayShowHomeEnabled(true)
        Realm.getDefaultInstance()
            .addChangeListener { updateFavoriteButton() }
        updateFavoriteButton()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun onClickFavoriteButton() {
        when (binding.favoriteButton.text) {
            getString(R.string.ADD_FAVORITE) -> {
                addFavorite(shopData)
            }
            getString(R.string.REMOVE_FAVORITE) -> {
                showConfirmDeleteFavoriteDialog(shopData.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        val isFavorite = FavoriteShop.findBy(shopData.id) != null
        binding.favoriteButton.text =
            if (isFavorite) getString(R.string.REMOVE_FAVORITE) else getString(R.string.ADD_FAVORITE)
    }
}