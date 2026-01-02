package com.example.demo;

public class KeyValueData {
    private String Key, Value;

    public KeyValueData() {}

    public KeyValueData(String Key, String Value) {
        this.Key = Key;
        this.Value = Value;
    }

    public String get_Key() { return Key; }
    public String get_Values() { return Value; }

    public void set_Ket(String Key) {
        this.Key = Key;
    }
    public void set_Values(String Value){
        this.Value = Value;
    }

}
