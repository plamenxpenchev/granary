CREATE TABLE db_version (
    version smallint NOT NULL
);

CREATE TABLE items (
    id varchar(255) PRIMARY KEY,
    parent_id varchar(255) REFERENCES items(id),
    hierarchy_level smallint NOT NULL,
    is_leaf boolean NOT NULL,
    name varchar(255) NOT NULL
);
CREATE INDEX items_parent_id_idx ON items (parent_id);
CREATE INDEX items_name_idx ON items (name);

CREATE TABLE price_tags (
    item_id varchar(255) PRIMARY KEY REFERENCES items(id) ON DELETE CASCADE,
    price numeric NOT NULL,
    discount_percentage smallint
);
CREATE INDEX pricate_tags_price_idx ON price_tags (price);