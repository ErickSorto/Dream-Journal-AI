package org.ballistic.dreamjournalai.shared.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private object StoreInitialPageNavType : NavType<StoreInitialPage>(false) {
    override val name: String = StoreInitialPage::class.qualifiedName.orEmpty()

    override fun put(bundle: SavedState, key: String, value: StoreInitialPage) {
        bundle.write { putString(key, value.name) }
    }

    override fun get(bundle: SavedState, key: String): StoreInitialPage? {
        return bundle.read { getStringOrNull(key)?.let(::parseValue) }
    }

    override fun parseValue(value: String): StoreInitialPage {
        return enumValueOf<StoreInitialPage>(value)
    }

    override fun serializeAsValue(value: StoreInitialPage): String {
        return value.name
    }
}

internal val storeRouteTypeMap: Map<KType, NavType<*>> = mapOf(
    typeOf<StoreInitialPage>() to StoreInitialPageNavType
)
