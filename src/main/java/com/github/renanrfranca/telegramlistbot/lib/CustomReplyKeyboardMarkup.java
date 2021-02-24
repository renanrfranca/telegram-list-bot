package com.github.renanrfranca.telegramlistbot.lib;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomReplyKeyboardMarkup extends ReplyKeyboardMarkup implements ReplyKeyboard {
    public CustomReplyKeyboardMarkup(List<KeyboardRow> keyboard, Boolean resizeKeyboard, Boolean oneTimeKeyboard, Boolean selective, Boolean forceReply) {
        super(keyboard, resizeKeyboard, oneTimeKeyboard, selective);
        this.forceReply = forceReply;
    }

    @JsonProperty("force_reply")
    private Boolean forceReply;

    public Boolean getForceReply() {
        return forceReply;
    }

    @JsonProperty("force_reply")
    public void setForceReply(Boolean selective) {
        this.forceReply = selective;
    }

    public static CustomReplyKeyboardMarkupBuilder getBuilder() {
        return new CustomReplyKeyboardMarkupBuilder();
    }

    public static class CustomReplyKeyboardMarkupBuilder {
        private ArrayList<KeyboardRow> keyboard;
        private Boolean resizeKeyboard;
        private Boolean oneTimeKeyboard;
        private Boolean selective;
        private Boolean forceReply;

        CustomReplyKeyboardMarkupBuilder() {
        }

        public CustomReplyKeyboardMarkupBuilder keyboardRow(KeyboardRow keyboardRow) {
            if (this.keyboard == null) {
                this.keyboard = new ArrayList();
            }

            this.keyboard.add(keyboardRow);
            return this;
        }

        @JsonProperty("keyboard")
        public CustomReplyKeyboardMarkupBuilder keyboard(Collection<? extends KeyboardRow> keyboard) {
            if (keyboard == null) {
                throw new NullPointerException("keyboard cannot be null");
            } else {
                if (this.keyboard == null) {
                    this.keyboard = new ArrayList();
                }

                this.keyboard.addAll(keyboard);
                return this;
            }
        }

        public CustomReplyKeyboardMarkupBuilder clearKeyboard() {
            if (this.keyboard != null) {
                this.keyboard.clear();
            }

            return this;
        }

        @JsonProperty("resize_keyboard")
        public CustomReplyKeyboardMarkupBuilder resizeKeyboard(Boolean resizeKeyboard) {
            this.resizeKeyboard = resizeKeyboard;
            return this;
        }

        @JsonProperty("one_time_keyboard")
        public CustomReplyKeyboardMarkupBuilder oneTimeKeyboard(Boolean oneTimeKeyboard) {
            this.oneTimeKeyboard = oneTimeKeyboard;
            return this;
        }

        @JsonProperty("selective")
        public CustomReplyKeyboardMarkupBuilder selective(Boolean selective) {
            this.selective = selective;
            return this;
        }

        @JsonProperty("force_reply")
        public CustomReplyKeyboardMarkupBuilder forceReply(Boolean forceReply) {
            this.forceReply = forceReply;
            return this;
        }

        public CustomReplyKeyboardMarkup build() {
            List keyboard;
            switch(this.keyboard == null ? 0 : this.keyboard.size()) {
                case 0:
                    keyboard = Collections.emptyList();
                    break;
                case 1:
                    keyboard = Collections.singletonList((KeyboardRow)this.keyboard.get(0));
                    break;
                default:
                    keyboard = Collections.unmodifiableList(new ArrayList(this.keyboard));
            }

            return new CustomReplyKeyboardMarkup(keyboard, this.resizeKeyboard, this.oneTimeKeyboard, this.selective, this.forceReply);
        }

        public String toString() {
            return "CustomReplyKeyboardMarkupBuilder(keyboard=" + this.keyboard + ", resizeKeyboard=" + this.resizeKeyboard + ", oneTimeKeyboard=" + this.oneTimeKeyboard + ", selective=" + this.selective + ", forceReply=" + this.forceReply + ")";
        }
    }
}
