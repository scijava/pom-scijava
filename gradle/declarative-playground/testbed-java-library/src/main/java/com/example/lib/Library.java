package com.example.lib;

import com.example.utils.Utils;
import com.google.common.collect.ImmutableList;

public class Library {
    public Iterable<String> getMessages() {
        // Verify that Guava is available
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("Hello from Java " + System.getProperty("java.version"));

        // Verify that the Java library is available
        Utils utils = new Utils();
        builder.add(utils.getWelcome());

        return builder.build();
    }
}
