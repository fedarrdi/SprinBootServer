package com.example.demo;

public class KeyValueData {
    private String key;
    private String values;

    public KeyValueData() {}

    public KeyValueData(String key, String values) {
        this.key = key;
        this.values = values;
    }

    public String getKey() { return key; }
    public String getValues() { return values; }

    public void setKey(String key) {
        this.key = key;
    }
    public void setValues(String values){
        this.values = values;
    }

}
