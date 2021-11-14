package jp.techacademy.takafumi.shouji.apiapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import jp.techacademy.takafumi.shouji.apiapp.databinding.FragmentApiBinding
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class ApiFragment : Fragment() {

    companion object {
        private const val COUNT = 20 // 1回のAPIで取得する件数
    }

    private var _binding: FragmentApiBinding? = null
    private val binding get() = _binding!!
    private val apiAdapter by lazy { ApiAdapter(requireContext()) }
    private val handler = Handler(Looper.getMainLooper())

    // Fragment -> Activity にFavoriteの変更を通知する
    private var fragmentCallback: FragmentCallback? = null

    private var page = 0
    private var searchString = ""

    // Apiでデータを読み込み中ですフラグ。追加ページの読み込みの時にこれがないと、連続して読み込んでしまうので、それの制御のため
    private var isLoading = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentCallback) {
            fragmentCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ApiAdapterのお気に入り追加、削除用のメソッドの追加を行う
        apiAdapter.apply {
            onClickAddFavorite = { // Adapterの処理をそのままActivityに通知する
                fragmentCallback?.onAddFavorite(it)
            }
            onClickDeleteFavorite = { // Adapterの処理をそのままActivityに通知する
                fragmentCallback?.onDeleteFavorite(it.id)
            }
            // Itemをクリックしたとき
            onClickItem = {
                fragmentCallback?.onClickItem(FavoriteShop.fromShop(it))
            }
        }
        // RecyclerViewの初期化
        binding.recyclerView.apply {
            adapter = apiAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示
            addOnScrollListener(object :
                RecyclerView.OnScrollListener() { // Scrollを検知するListenerを実装する。これによって、RecyclerViewの下端に近づいた時に次のページを読み込んで、下に付け足す
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) { // dx はx軸方向の変化量(横) dy はy軸方向の変化量(縦) ここではRecyclerViewは縦方向なので、dyだけ考慮する
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy == 0) { // 縦方向の変化量(スクロール量)が0の時は動いていないので何も処理はしない
                        return
                    }
                    val totalCount = apiAdapter.itemCount // RecyclerViewの現在の表示アイテム数
                    val lastVisibleItem =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition() // RecyclerViewの現在見えている最後のViewHolderのposition
                    // totalCountとlastVisibleItemから全体のアイテム数のうちどこまでが見えているかがわかる(例:totalCountが20、lastVisibleItemが15の時は、現在のスクロール位置から下に5件見えていないアイテムがある)
                    // 一番下にスクロールした時に次の20件を表示する等の実装が可能になる。
                    // ユーザビリティを考えると、一番下にスクロールしてから追加した場合、一度スクロールが止まるので、ユーザーは気付きにくい
                    // ここでは、一番下から5番目を表示した時に追加読み込みする様に実装する
                    if (!isLoading && lastVisibleItem >= totalCount - 6) { // 読み込み中でない、かつ、現在のスクロール位置から下に5件見えていないアイテムがある
                        updateData(isAdd = true)
                    }
                }
            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) updateData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        searchString = getString(R.string.api_keyword)
        updateData()
    }

    private fun updateData(searchWord: String = "", isAdd: Boolean = false) {
        if (isLoading) {
            return
        } else {
            isLoading = true
        }
        if (isAdd) {
            page++
        } else {
            page = 0
        }
        if (!searchWord.isNullOrEmpty()) {
            searchString = searchWord
        }
        val start = page * COUNT + 1
        val url = StringBuilder()
            .append(getString(R.string.base_url)) // https://webservice.recruit.co.jp/hotpepper/gourmet/v1/
            .append("?key=").append(getString(R.string.api_key)) // Apiを使うためのApiKey
            .append("&start=").append(start) // 何件目からのデータを取得するか
            .append("&count=").append(COUNT) // 1回で20件取得する
            .append("&keyword=")
            .append(searchString) // お店の検索ワード。ここでは例として「ランチ」を検索
            .append("&format=json") // ここで利用しているAPIは戻りの形をxmlかjsonが選択することができる。Androidで扱う場合はxmlよりもjsonの方が扱いやすいので、jsonを選択
            .toString()
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { // Error時の処理
                e.printStackTrace()
                handler.post {
                    updateRecyclerView(listOf(), isAdd)
                }
                isLoading = false // 読み込み中フラグを折る
            }

            override fun onResponse(call: Call, response: Response) { // 成功時の処理
                var list = listOf<Shop>()
                response.body?.string()?.also {
                    val apiResponse = Gson().fromJson(it, ApiResponse::class.java)
                    list = apiResponse.results.shop
                }
                handler.post {
                    updateRecyclerView(list, isAdd)
                }
                isLoading = false // 読み込み中フラグを折る
            }
        })
    }

    fun updateView() { // お気に入りが削除されたときの処理（Activityからコールされる）
        // RecyclerViewのAdapterに対して再描画のリクエストをする
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun updateRecyclerView(list: List<Shop>, isAdd: Boolean) {
        if (isAdd) {
            apiAdapter.add(list)
        } else {
            apiAdapter.refresh(list)
        }
        binding.swipeRefreshLayout.isRefreshing = false // SwipeRefreshLayoutのくるくるを消す
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}