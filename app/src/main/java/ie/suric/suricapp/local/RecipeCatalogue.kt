package ie.suric.suricapp.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Collator
import java.util.Locale

private fun JSONObject.optionalNonBlankString(name: String): String? =
    (opt(name) as? String)?.takeIf(String::isNotBlank)

/** Canonical offline recipes and their collection memberships. Collection rows point to recipe IDs. */
data class OfflineRecipe(
    val id: String,
    val name: String,
    val englishName: String?,
    val country: String,
    val region: String?,
    val description: String,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val sides: String,
    val substitutions: String,
    val storage: String,
    val safety: String,
    val tags: List<String>,
    val revision: Int,
    val articleId: String?,
    val imageAssetNames: List<String> = emptyList()
) {
    val searchableText: String get() = "$name ${englishName.orEmpty()} $country ${region.orEmpty()} $description ${ingredients.joinToString(" ")} ${tags.joinToString(" ")}"
}

data class RecipeCollection(val id: String, val title: String, val recipeIds: List<String>)

data class RecipeCatalogue(val recipes: List<OfflineRecipe>, val collections: List<RecipeCollection>) {
    fun sortedRecipes(candidates: List<OfflineRecipe>): List<OfflineRecipe> {
        val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
        return candidates.sortedWith { left, right ->
            val byCountry = collator.compare(left.country, right.country)
            if (byCountry != 0) byCountry else {
                val byName = collator.compare(left.name, right.name)
                if (byName != 0) byName else left.id.compareTo(right.id)
            }
        }
    }

    fun recipesIn(collection: RecipeCollection): List<OfflineRecipe> {
        val byId = recipes.associateBy(OfflineRecipe::id)
        return sortedRecipes(collection.recipeIds.mapNotNull(byId::get))
    }
}

object OfflineRecipeCatalogue {
    fun load(context: Context, articles: List<OfflineArticle>): RecipeCatalogue {
        val manifest = context.assets.open("recipe-catalogue-manifest.json").bufferedReader().use { JSONObject(it.readText()) }
        require(manifest.optInt("version") == 1) { "This recipe catalogue manifest version is not supported." }
        val extensions = manifest.getJSONArray("catalogues").let { values -> (0 until values.length()).map(values::getString) }
        require(extensions.distinct().size == extensions.size && extensions.all { it.matches(Regex("[a-z0-9-]{1,100}\\.json")) }) {
            "Recipe catalogue manifest contains an invalid or duplicate asset name."
        }
        val roots = (listOf("recipe-catalogue.json") + extensions).map { asset ->
            context.assets.open(asset).bufferedReader().use { JSONObject(it.readText()) }
        }
        require(roots.all { it.optInt("version") == 1 }) { "This recipe catalogue version is not supported." }
        val recipes = roots.flatMap { root -> root.getJSONArray("recipes").let { items ->
            (0 until items.length()).map { index ->
                val item = items.getJSONObject(index)
                val articleId = item.optionalNonBlankString("articleId")
                val article = articleId?.let { id -> articles.firstOrNull { it.id == id } }
                    ?: if (articleId == null) null else error("Recipe ${item.optString("id")} refers to a missing article.")
                OfflineRecipe(
                    id = item.getString("id"), name = item.getString("name"),
                    englishName = item.optionalNonBlankString("englishName"),
                    country = item.getString("country"), region = item.optionalNonBlankString("region"),
                    description = item.getString("description"), prepMinutes = item.getInt("prepMinutes"),
                    cookMinutes = item.getInt("cookMinutes"), servings = item.getInt("servings"),
                    difficulty = item.getString("difficulty"),
                    ingredients = item.getJSONArray("ingredients").let { values -> (0 until values.length()).map(values::getString) },
                    instructions = item.getJSONArray("instructions").let { values -> (0 until values.length()).map(values::getString) },
                    sides = item.getString("sides"), substitutions = item.getString("substitutions"),
                    storage = item.getString("storage"), safety = item.getString("safety"),
                    tags = item.getJSONArray("tags").let { values -> (0 until values.length()).map(values::getString) },
                    revision = item.getInt("revision"), articleId = articleId,
                    imageAssetNames = item.optJSONArray("imageAssets")?.let { values -> (0 until values.length()).map(values::getString) }.orEmpty()
                )
            }
        } }
        val collections = roots.flatMap { root -> (root.optJSONArray("collections") ?: JSONArray()).let { items ->
            (0 until items.length()).map { index ->
                val item = items.getJSONObject(index)
                RecipeCollection(item.getString("id"), item.getString("title"), item.getJSONArray("recipeIds").let { ids -> (0 until ids.length()).map(ids::getString) })
            }
        } }.sortedWith { left, right ->
            val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
            val byTitle = collator.compare(left.title, right.title)
            if (byTitle != 0) byTitle else left.id.compareTo(right.id)
        }
        require(recipes.map(OfflineRecipe::id).distinct().size == recipes.size) { "Recipe catalogue contains duplicate IDs." }
        require(collections.map(RecipeCollection::id).distinct().size == collections.size) { "Recipe catalogue contains duplicate collection IDs." }
        require(recipes.all { it.id.isNotBlank() && it.country.isNotBlank() && it.revision > 0 && it.ingredients.isNotEmpty() && it.instructions.isNotEmpty() }) { "Recipe catalogue contains an incomplete recipe." }
        val ids = recipes.map(OfflineRecipe::id).toSet()
        require(collections.all { collection -> collection.recipeIds.distinct().size == collection.recipeIds.size && collection.recipeIds.all(ids::contains) }) { "Recipe collection contains duplicate or missing recipes." }
        return RecipeCatalogue(recipes, collections)
    }
}
