package net.cufufy.pronouns.common.placeholder;

import net.cufufy.pronouns.common.platform.CommandSender;

import java.util.function.BiFunction;

public record Placeholder(String name, BiFunction<CommandSender, String, Result> function) {
    public record Result(boolean success, String message) {
        static Result fail(String value) {
            return new Result(false, value);
        }
        static Result of(String value) {
            return new Result(true, value);
        }
    }
}
