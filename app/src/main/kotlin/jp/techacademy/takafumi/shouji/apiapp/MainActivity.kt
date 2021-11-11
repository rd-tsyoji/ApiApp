package jp.techacademy.takafumi.shouji.apiapp

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import jp.techacademy.takafumi.shouji.apiapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity(), FragmentCallback {

    companion object {
        private const val VIEW_PAGER_POSITION_API = 0
        private const val VIEW_PAGER_POSITION_FAVORITE = 1
    }

    private lateinit var binding: ActivityMainBinding
    private val viewPagerAdapter by lazy { ViewPagerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager2の初期化
        binding.viewPager2.apply {
            adapter = viewPagerAdapter
            orientation =
                ViewPager2.ORIENTATION_HORIZONTAL // スワイプの向き横（ORIENTATION_VERTICAL を指定すれば縦スワイプで実装可）
            offscreenPageLimit = viewPagerAdapter.itemCount // ViewPager2で保持する画面数
        }

        // TabLayoutの初期化
        // TabLayoutとViewPager2を紐づける
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.setText(viewPagerAdapter.titleIds[position]) // TabLayoutのTextを指定する
        }.attach()
    }

    override fun onAddFavorite(shop: Shop) {
        addFavorite(FavoriteShop.fromShop(shop))
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
    }

    override fun onDeleteFavorite(id: String) {
        showConfirmDeleteFavoriteDialog(id)
    }

    override fun onClickItem(favoriteShop: FavoriteShop) {
        WebViewActivity.start(this, favoriteShop)
    }

    override fun deleteFavorite(id: String) {
        super.deleteFavorite(id)
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_API] as ApiFragment).updateView()
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
    }
}