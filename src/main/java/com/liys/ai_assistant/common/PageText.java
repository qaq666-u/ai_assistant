package com.liys.ai_assistant.common;

/**
 * Represents a record class for storing text content along with its page number.
 * This record is used to encapsulate the relationship between a page number and its corresponding text.
 */
public record PageText(int page, String text) {
}
