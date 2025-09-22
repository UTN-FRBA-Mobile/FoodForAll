package ar.edu.utn.frba.mobile.foodforall.ui.model

enum class DietaryRestriction(val emoji: String, val description: String, val key: String) {
    VEGETARIAN("🌽", "Vegetariano", "vegetarian"),
    VEGAN("🌱", "Vegano", "vegan"),
    CELIAC("🥛", "Celíaco", "celiac"),
    SIBO("🩺", "SIBO", "sibo"),
    GENERAL("", "General", "general");

    companion object {
        fun fromKey(key: String): DietaryRestriction? {
            return entries.find { it.key == key }
        }
    }
}