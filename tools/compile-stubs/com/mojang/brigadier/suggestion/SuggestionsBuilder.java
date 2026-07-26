package com.mojang.brigadier.suggestion;
import java.util.concurrent.CompletableFuture;
public class SuggestionsBuilder {
    public SuggestionsBuilder(String input, int start) {}
    public String getInput() { return null; }
    public int getStart() { return 0; }
    public SuggestionsBuilder suggest(String text) { return this; }
    public Suggestions build() { return null; }
    public CompletableFuture<Suggestions> buildFuture() { return null; }
}
