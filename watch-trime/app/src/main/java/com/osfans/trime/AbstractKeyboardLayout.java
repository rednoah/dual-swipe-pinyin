package com.osfans.trime;


import android.content.Context;
import android.graphics.Typeface;
import android.support.wearable.view.BoxInsetLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.osfans.trime.model.Punctuation;

public abstract class AbstractKeyboardLayout extends BoxInsetLayout {


    protected View layout;
    protected TextView editor;


    protected String buffer;
    protected int highlightColor;

    protected int composingStart = 0;
    protected int highlightStart = 0;


    public AbstractKeyboardLayout(Context context, int layout) {
        super(context);

        this.layout = LayoutInflater.from(context).inflate(layout, this, true);
        this.editor = (TextView) this.layout.findViewById(getEditorLayout());


        this.highlightColor = getResources().getColor(R.color.editor_highlight_fg, getContext().getTheme());

        // init text buffer
        updateTextBuffer("");

        // hook up editor touch input for SPACE and BACKSPACE
        editor.setOnTouchListener((v, evt) -> {
            // listen only for button press events
            if (evt.getAction() == MotionEvent.ACTION_DOWN) {
                onEditorClick(v, evt);
                return true;
            }
            return false;
        });

        // register listener for all buttons
        getButtons().forEach(b -> b.setOnClickListener(v -> enterKey(b)));
    }


    protected abstract int getEditorLayout();


    protected abstract int[] getButtonGroups();


    protected Stream<Button> getButtons() {
        return IntStream.of(getButtonGroups())
                .mapToObj(this.layout::findViewById)
                .flatMap(v -> {
                    if (v instanceof ViewGroup) {
                        ViewGroup g = (ViewGroup) v;
                        return IntStream.range(0, g.getChildCount()).mapToObj(g::getChildAt);
                    }
                    if (v instanceof Button) {
                        return Stream.of(v);
                    }
                    return Stream.empty();
                })
                .map(Button.class::cast);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


    public void onEditorClick(View view, MotionEvent event) {
        // UNUSED
    }

    public void enterKey(Button button) {
        String key = button.getText().toString();

        switch (key) {
            case Symbols.ENTER:
            case Symbols.KEYBOARD:
            case Symbols.OPTION:
                keyPressed(key, InputType.CONTROL_KEY);
                break;
            case Symbols.BACKSPACE:
                keyPressed(key, InputType.DELETE_LETTER);
                break;
            default:
                keyPressed(key, InputType.ENTER_LETTER);
                break;
        }
    }


    public void enterSuggestion(String text) {
        keyPressed(text, InputType.ENTER_WORD);
    }


    public void enterBackspace() {
        if (buffer.isEmpty()) {
            return;
        }
        keyPressed(Symbols.BACKSPACE, InputType.DELETE_LETTER);
    }


    public void enterSpace() {
        if (buffer.isEmpty()) {
            return;
        }

        keyPressed(Punctuation.DOT.toString(), InputType.ENTER_LETTER);
    }


    public void keyPressed(String key, InputType type) {
        switch (type) {
            case DELETE_LETTER:
                popHistory();
                break;
            case ENTER_LETTER:
                pushHistory();
                updateTextBuffer(applyLetter(key, buffer));
                break;
            case ENTER_WORD:
                if (getComposingBuffer().chars().noneMatch(Character::isIdeographic)) {
                    pushHistory();
                }
                updateTextBuffer(applyWord(key, buffer));
                break;
            case CONTROL_KEY:
                switch (key) {
                    case Symbols.ENTER:
                        post(this::submit);
                        break;
                }
                break;
        }


        if (hapticFeedback != null) {
            hapticFeedback.feedback();
        }


        if (recorder != null && type != InputType.CONTROL_KEY) {
            recorder.record(key, buffer);
        }
    }


    protected String applyLetter(String key, String buffer) {
        return buffer + key;
    }


    protected String applyWord(String word, String buffer) {
        if (buffer.length() > 0) {
            for (int i = 0; i < buffer.length(); i++) {
                if (Character.isIdeographic(buffer.charAt(i))) {
                    continue;
                }
                return buffer.substring(0, i) + word;
            }
        }
        return buffer + word;
    }


    protected void setComposingBuffer(String buffer) {
        updateTextBuffer(this.buffer.substring(0, composingStart) + buffer);
    }


    protected void updateTextBuffer(String buffer) {
        this.buffer = buffer;

        if (highlightStart >= 0 && highlightStart < buffer.length()) {
            editor.setText(highlightTail(buffer, highlightStart));
        } else {
            editor.setText(buffer);
        }
    }


    public void markComposingStart() {
        this.composingStart = buffer.length();

        // clear key stroke history and clear character by character when deleting previously committed characters
        this.history.clear();

        // update composition highlight
        updateTextBuffer(buffer);
    }

    public void markHighlightStart() {
        this.highlightStart = buffer.length();

        // update composition highlight
        updateTextBuffer(buffer);
    }

    protected String getComposingBuffer() {
        if (composingStart < 0 || composingStart > buffer.length()) {
            return "";
        }

        return buffer.substring(composingStart);
    }


    protected String getHighlightBuffer() {
        if (highlightStart < 0 || highlightStart > buffer.length()) {
            return "";
        }

        return buffer.substring(highlightStart);
    }


    protected Spanned highlightTail(String text, int from) {
        SpannableString span = new SpannableString(text);
        span.setSpan(new ForegroundColorSpan(highlightColor), from, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    protected Spanned highlightKey(String key) {
        SpannableString span = new SpannableString(key);
        span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    protected void highlight(String key, Button button, boolean enabled) {
        if (enabled) {
            button.setText(highlightKey(key));
        } else {
            button.setText(key);
        }
    }


    protected HapticFeedback hapticFeedback;
    protected Recorder recorder;

    private final List<Consumer<String>> submitListener = new ArrayList<>();


    public void submit() {
        submitListener.forEach(c -> c.accept(buffer.trim()));
        clear();
    }


    public void clear() {
        buffer = "";

        markComposingStart();
        markHighlightStart();
    }


    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    public void setHapticFeedback(HapticFeedback hapticFeedback) {
        this.hapticFeedback = hapticFeedback;
    }

    public void addSubmitListener(Consumer<String> listener) {
        submitListener.add(listener);
    }


    private final Stack<State> history = new Stack<>();


    public void pushHistory() {
        history.push(new State(buffer, composingStart, highlightStart));
    }


    public void popHistory() {
        if (history.isEmpty()) {
            deleteLastCharacter();
            return;
        }


        State prev = history.pop();
        buffer = prev.buffer;
        composingStart = prev.composingStart;
        highlightStart = prev.highlightStart;

        updateTextBuffer(buffer);
    }


    public void deleteLastCharacter() {
        if (buffer.isEmpty()) {
            return;
        }

        buffer = buffer.substring(0, buffer.length() - 1);
        markComposingStart();
        markHighlightStart();

        updateTextBuffer(buffer);
    }


    public void setText(String s) {
        buffer = s;

        markComposingStart();
        markHighlightStart();
    }


    public static class State {
        public final String buffer;
        public final int composingStart;
        public final int highlightStart;

        public State(String buffer, int composingStart, int highlightStart) {
            this.buffer = buffer;
            this.composingStart = composingStart;
            this.highlightStart = highlightStart;
        }
    }


}
