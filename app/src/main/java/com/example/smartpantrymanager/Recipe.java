
package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recipe {

    private final String name;
    private final List<String> ingredients;
    private final List<IngredientRequirement> requirements;
    private final String instructions;

    public Recipe(
            String name,
            List<String> ingredients,
            String instructions) {

        this.name = name;
        this.ingredients = new ArrayList<>(ingredients);
        this.instructions = instructions;
        this.requirements = createDefaultRequirements(ingredients);
    }

    public Recipe(
            String name,
            List<IngredientRequirement> requirements,
            String instructions,
            boolean quantityAware) {

        this.name = name;
        this.requirements = new ArrayList<>(requirements);
        this.ingredients = new ArrayList<>();

        for (IngredientRequirement requirement : requirements) {
            this.ingredients.add(requirement.getName());
        }

        this.instructions = instructions;
    }

    private List<IngredientRequirement> createDefaultRequirements(
            List<String> ingredientNames) {

        List<IngredientRequirement> result = new ArrayList<>();

        for (String ingredient : ingredientNames) {
            result.add(new IngredientRequirement(
                    ingredient, 1, "item"
            ));
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public List<IngredientRequirement> getRequirements() {
        return new ArrayList<>(requirements);
    }

    public String getInstructions() {
        return instructions;
    }

    public static class IngredientRequirement {

        private final String name;
        private final double quantity;
        private final String unit;

        public IngredientRequirement(
                String name,
                double quantity,
                String unit) {

            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }

    private static IngredientRequirement req(
            String name, double quantity, String unit) {

        return new IngredientRequirement(name, quantity, unit);
    }

    public static List<Recipe> getSampleRecipes() {
        return Arrays.asList(

                new Recipe("Tomato Pasta", Arrays.asList(
                        req("pasta", 200, "g"),
                        req("tomato", 2, "item"),
                        req("onion", 1, "item"),
                        req("garlic", 2, "clove")
                ), "1. Cook the pasta according to the package instructions.\n"
                        + "2. Chop the tomato and onion, then cook with garlic.\n"
                        + "3. Mix the sauce with the pasta and serve.", true),

                new Recipe("Vegetable Omelette", Arrays.asList(
                        req("eggs", 2, "item"),
                        req("onion", 0.5, "item"),
                        req("tomato", 1, "item"),
                        req("cheese", 30, "g")
                ), "1. Beat the eggs in a bowl.\n"
                        + "2. Chop the onion and tomato.\n"
                        + "3. Cook the vegetables in a pan, add the eggs and cheese, and cook until set.", true),

                new Recipe("Fried Rice", Arrays.asList(
                        req("rice", 200, "g"),
                        req("eggs", 2, "item"),
                        req("carrot", 1, "item"),
                        req("onion", 1, "item")
                ), "1. Cook the rice and allow it to cool.\n"
                        + "2. Chop the carrot and onion.\n"
                        + "3. Cook the vegetables, add the eggs, then stir in the rice and heat thoroughly.", true),

                new Recipe("Potato and Onion Hash", Arrays.asList(
                        req("potato", 2, "item"),
                        req("onion", 1, "item"),
                        req("oil", 15, "ml"),
                        req("salt", 2, "g")
                ), "1. Wash and dice the potatoes.\n"
                        + "2. Chop the onion.\n"
                        + "3. Heat oil in a pan and cook the potatoes and onion until tender.\n"
                        + "4. Season with salt and serve.", true),

                new Recipe("Banana Pancakes", Arrays.asList(
                        req("banana", 1, "item"),
                        req("eggs", 2, "item"),
                        req("flour", 100, "g"),
                        req("milk", 100, "ml")
                ), "1. Mash the banana.\n"
                        + "2. Mix with eggs, flour and milk.\n"
                        + "3. Pour small portions into a lightly oiled pan.\n"
                        + "4. Cook on both sides until golden.", true),

                new Recipe("Chicken and Rice", Arrays.asList(
                        req("chicken", 200, "g"),
                        req("rice", 200, "g"),
                        req("onion", 1, "item"),
                        req("garlic", 2, "clove")
                ), "1. Chop the onion and garlic.\n"
                        + "2. Cook the chicken thoroughly in a pan with the onion and garlic.\n"
                        + "3. Cook the rice separately.\n"
                        + "4. Serve the chicken with the rice.", true),

                new Recipe("Creamy Mushroom Pasta", Arrays.asList(
                        req("pasta", 200, "g"),
                        req("mushroom", 100, "g"),
                        req("milk", 150, "ml"),
                        req("garlic", 2, "clove")
                ), "1. Cook the pasta according to the package instructions.\n"
                        + "2. Slice and cook the mushrooms with garlic.\n"
                        + "3. Add milk and simmer gently.\n"
                        + "4. Stir in the pasta and serve.", true),

                new Recipe("Cheese Sandwich", Arrays.asList(
                        req("bread", 2, "slice"),
                        req("cheese", 30, "g"),
                        req("butter", 10, "g")
                ), "1. Spread butter on the bread.\n"
                        + "2. Place cheese between two slices.\n"
                        + "3. Toast in a pan until golden and the cheese melts.", true),

                new Recipe("Tomato and Egg Stir-Fry", Arrays.asList(
                        req("eggs", 2, "item"),
                        req("tomato", 2, "item"),
                        req("onion", 1, "item"),
                        req("oil", 15, "ml")
                ), "1. Beat the eggs in a bowl.\n"
                        + "2. Chop the tomato and onion.\n"
                        + "3. Cook the onion and tomato in oil.\n"
                        + "4. Add the eggs and stir until fully cooked.", true),

                new Recipe("Vegetable Soup", Arrays.asList(
                        req("potato", 1, "item"),
                        req("carrot", 2, "item"),
                        req("onion", 1, "item"),
                        req("salt", 2, "g")
                ), "1. Wash and chop the vegetables.\n"
                        + "2. Add them to a pot of water.\n"
                        + "3. Simmer until all vegetables are tender.\n"
                        + "4. Season with salt and serve hot.", true),

                new Recipe("Garlic Fried Potatoes", Arrays.asList(
                        req("potato", 2, "item"),
                        req("garlic", 2, "clove"),
                        req("oil", 15, "ml"),
                        req("salt", 2, "g")
                ), "1. Wash and cut the potatoes into small pieces.\n"
                        + "2. Heat oil in a pan.\n"
                        + "3. Add the potatoes and cook until golden and tender.\n"
                        + "4. Add garlic and salt, then cook briefly before serving.", true),

                new Recipe("Egg Fried Noodles", Arrays.asList(
                        req("noodles", 200, "g"),
                        req("eggs", 2, "item"),
                        req("carrot", 1, "item"),
                        req("onion", 1, "item")
                ), "1. Cook the noodles according to the package instructions.\n"
                        + "2. Chop the carrot and onion.\n"
                        + "3. Stir-fry the vegetables in a pan.\n"
                        + "4. Add the eggs and cook thoroughly, then mix in the noodles.", true),

                new Recipe("Bean and Tomato Stew", Arrays.asList(
                        req("beans", 200, "g"),
                        req("tomato", 2, "item"),
                        req("onion", 1, "item"),
                        req("garlic", 2, "clove")
                ), "1. Chop the onion, tomato and garlic.\n"
                        + "2. Cook the onion and garlic in a pot.\n"
                        + "3. Add the tomato and cooked beans.\n"
                        + "4. Simmer until the flavours combine and serve.", true),

                new Recipe("Tuna Pasta", Arrays.asList(
                        req("pasta", 200, "g"),
                        req("tuna", 100, "g"),
                        req("tomato", 1, "item"),
                        req("onion", 1, "item")
                ), "1. Cook the pasta according to the package instructions.\n"
                        + "2. Chop and cook the onion and tomato.\n"
                        + "3. Stir in the tuna and heat thoroughly.\n"
                        + "4. Mix with the pasta and serve.", true),

                new Recipe("French Toast", Arrays.asList(
                        req("bread", 2, "slice"),
                        req("eggs", 2, "item"),
                        req("milk", 60, "ml"),
                        req("butter", 10, "g")
                ), "1. Beat the eggs with the milk.\n"
                        + "2. Dip each slice of bread into the mixture.\n"
                        + "3. Melt butter in a pan.\n"
                        + "4. Cook both sides until golden and the egg is fully cooked.", true),

                new Recipe("Carrot and Potato Mash", Arrays.asList(
                        req("carrot", 2, "item"),
                        req("potato", 2, "item"),
                        req("butter", 15, "g"),
                        req("salt", 2, "g")
                ), "1. Peel and chop the potatoes and carrots.\n"
                        + "2. Boil until tender.\n"
                        + "3. Drain the water and mash the vegetables with butter.\n"
                        + "4. Season with salt and serve.", true),

                new Recipe("Chicken and Potato Curry", Arrays.asList(
                        req("chicken", 200, "g"),
                        req("potato", 2, "item"),
                        req("onion", 1, "item"),
                        req("tomato", 2, "item")
                ), "1. Chop the onion, tomato and potatoes.\n"
                        + "2. Cook the onion and chicken in a pot until the chicken is browned.\n"
                        + "3. Add tomato, potatoes, curry seasoning and water.\n"
                        + "4. Simmer until the chicken is thoroughly cooked and the potatoes are tender.", true),

                new Recipe("Banana Smoothie", Arrays.asList(
                        req("banana", 1, "item"),
                        req("milk", 250, "ml"),
                        req("yogurt", 100, "g")
                ), "1. Peel and slice the banana.\n"
                        + "2. Add the banana, milk and yogurt to a blender.\n"
                        + "3. Blend until smooth.\n"
                        + "4. Pour into a glass and serve immediately.", true),

                new Recipe("Tomato and Cheese Toast", Arrays.asList(
                        req("bread", 2, "slice"),
                        req("tomato", 1, "item"),
                        req("cheese", 30, "g"),
                        req("butter", 10, "g")
                ), "1. Slice the tomato and cheese.\n"
                        + "2. Butter the bread lightly.\n"
                        + "3. Add the tomato and cheese.\n"
                        + "4. Toast until the bread is golden and the cheese melts.", true),

                new Recipe("Simple Rice and Beans", Arrays.asList(
                        req("rice", 200, "g"),
                        req("beans", 200, "g"),
                        req("onion", 1, "item"),
                        req("tomato", 1, "item")
                ), "1. Cook the rice according to the package instructions.\n"
                        + "2. Chop the onion and tomato.\n"
                        + "3. Cook the onion and tomato in a pot, then add cooked beans.\n"
                        + "4. Simmer until heated through and serve with the rice.", true)
        );
    }
}
