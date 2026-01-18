package org.granary.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Item implements Result {

    private final String id;
    private final String parentId;
    private final short hierarchyLevel;
    private final boolean isLeaf;
    private final String name;

    private final BigDecimal price;
    private final Short discountPercentage;

    private Item(NodeBuilder builder) {
        this.id = builder.id;
        this.parentId = builder.parentId;
        this.hierarchyLevel = builder.hierarchyLevel;
        this.isLeaf = builder.isLeaf;
        this.name = builder.name;
        this.price = builder.price;
        this.discountPercentage = builder.discountPercentage;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    @JsonIgnore
    public short getHierarchyLevel() {
        return hierarchyLevel;
    }

    @JsonIgnore
    public boolean getIsLeaf() {
        return isLeaf;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Short getDiscountPercentage() {
        return discountPercentage;
    }

    public static Item mapRow(ResultSet resultSet) throws SQLException {
        return new Item.NodeBuilder()
                .withId(resultSet.getString("id"))
                .withParentId(resultSet.getString("parent_id"))
                .withHierarchyLevel(resultSet.getShort("hierarchy_level"))
                .withIsLeaf(resultSet.getBoolean("is_leaf"))
                .withName(resultSet.getString("name"))
                .withPrice(resultSet.getBigDecimal("price"))
                .withDiscountPercentage(resultSet.getShort("discount_percentage"))
                .build();
    }

    public static class NodeBuilder {

        private String id;
        private String parentId;
        private short hierarchyLevel;
        private boolean isLeaf;
        private String name;

        private BigDecimal price;
        private Short discountPercentage;

        public NodeBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public NodeBuilder withParentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public NodeBuilder withHierarchyLevel(short hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
            return this;
        }

        public NodeBuilder withIsLeaf(boolean isLeaf) {
            this.isLeaf = isLeaf;
            return this;
        }

        public NodeBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public NodeBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public NodeBuilder withDiscountPercentage(short discountPercentage) {
            this.discountPercentage = (discountPercentage == 0) ? null : discountPercentage;
            return this;
        }

        public NodeBuilder fromNode(Item item) {
            this.id = item.getId();
            this.parentId = item.getParentId();
            this.hierarchyLevel = item.getHierarchyLevel();
            this.isLeaf = item.getIsLeaf();
            this.name = item.getName();
            this.price = item.getPrice();
            this.discountPercentage = item.getDiscountPercentage();
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
