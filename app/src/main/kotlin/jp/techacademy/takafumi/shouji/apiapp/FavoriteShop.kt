package jp.techacademy.takafumi.shouji.apiapp

import java.io.Serializable
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class FavoriteShop : RealmObject(), Serializable {
    @PrimaryKey
    var id: String = ""
    var imageUrl: String = ""
    var name: String = ""
    var url: String = ""
    var address: String = ""

    companion object {
        // お気に入りのShopを全件取得
        fun findAll(): List<FavoriteShop> =
            Realm.getDefaultInstance().use { realm ->
                realm.where(FavoriteShop::class.java)
                    .findAll().let {
                        realm.copyFromRealm(it)
                    }
            }

        // お気に入りされているShopをidで検索して返す。お気に入りに登録されていなければnullで返す
        fun findBy(id: String): FavoriteShop? =
            Realm.getDefaultInstance().use { realm ->
                realm.where(FavoriteShop::class.java)
                    .equalTo(FavoriteShop::id.name, id)
                    .findFirst()?.let {
                        realm.copyFromRealm(it)
                    }
            }

        // お気に入り追加
        fun insert(favoriteShop: FavoriteShop) =
            Realm.getDefaultInstance().executeTransaction {
                it.insertOrUpdate(favoriteShop)
            }

        // idでお気に入りから削除する
        fun delete(id: String) =
            Realm.getDefaultInstance().use { realm ->
                realm.where(FavoriteShop::class.java)
                    .equalTo(FavoriteShop::id.name, id)
                    .findFirst()?.also { deleteShop ->
                        realm.executeTransaction {
                            deleteShop.deleteFromRealm()
                        }
                    }
            }

        fun fromShop(shop: Shop): FavoriteShop {
            return FavoriteShop().apply {
                id = shop.id
                imageUrl = shop.logoImage
                name = shop.name
                url = if (shop.couponUrls.sp.isNotEmpty()) shop.couponUrls.sp else shop.couponUrls.pc
                address = shop.address
            }
        }
    }
}