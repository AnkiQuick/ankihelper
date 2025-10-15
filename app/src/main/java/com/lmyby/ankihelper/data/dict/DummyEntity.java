package com.lmyby.ankihelper.data.dict;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Dummy entity required by Room for databases that don't use Room entities.
 * This table is never created or used - it exists only to satisfy Room's
 * requirement that @Database must have at least one entity.
 */
@Entity(tableName = "_dummy_room_entity")
public class DummyEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
}
