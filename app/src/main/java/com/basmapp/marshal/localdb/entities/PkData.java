package com.basmapp.marshal.localdb.entities;

import com.basmapp.marshal.localdb.annotations.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PkData extends ColumnData {

    private boolean isAutoIncrement;

    public PkData(Field field, PrimaryKey annotation, Method setter) {
        super(field, setter, annotation);
        this.isAutoIncrement = annotation.isAutoIncrement();
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }
}
