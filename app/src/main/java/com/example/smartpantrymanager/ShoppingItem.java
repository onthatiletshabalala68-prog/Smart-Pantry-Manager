
package com.example.smartpantrymanager;

public class ShoppingItem {

    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final boolean purchased;

    public ShoppingItem(int id, String name, boolean purchased) {
        this(id, name, 1.0, "item", purchased);
    }

    public ShoppingItem(
            int id,
            String name,
            double quantity,
            String unit,
            boolean purchased) {

        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit == null ? "item" : unit;
        this.purchased = purchased;
    }

    public int getId() {
        return id;
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

    public boolean isPurchased() {
        return purchased;
    }
}
