package ntu.csie.swipy;


import android.content.Context;
import android.os.AsyncTask;

import java.util.List;
import java.util.function.Consumer;

import woogle.spi.WoogleInputMethod;
import woogle.util.WoogleDatabase;

import static java.util.Collections.emptyList;


public class WoogleAutoComplete extends AutoComplete {


    private WoogleInputMethod woogle;


    public WoogleAutoComplete(Context context) {
        super(context);
    }


    public Result getSuggestions(String key, InputType type, String buffer) {
        if (type == InputType.DELETE_LETTER) {
            if (woogle != null) {
                woogle.clear();
                try {
                    buffer.chars().forEach(c -> woogle.keyPressed((char) c));
                } catch (Exception e) {
                    woogle.clear();
                }
                return new Result(woogle.getBuffer(), false, woogle.getCandidates());
            }
            return new Result(emptyList());
        }


        if (key.isEmpty() || buffer.isEmpty()) {
            if (woogle != null) {
                woogle.clear();
            }
            return new Result(emptyList());
        }


        // initialize
        if (woogle == null) {
            WoogleDatabase.load(context);
            woogle = new WoogleInputMethod();
        }


        // update prediction engine
        switch (type) {
            case ENTER_LETTER:
                key.chars().forEach(c -> woogle.keyPressed((char) c));
                break;
            case ENTER_WORD:
                if (woogle.select(key)) {
                    String s = woogle.result();
                    boolean commit = true;
                    if (s == null) {
                        s = woogle.getCompString();
                        commit = false;
                    }
                    return new Result(s, commit, woogle.getCandidates());
                }
                break;
        }


        List<String> suggestions = woogle.getCandidates();

        // Log.d("AutoComplete", "PINYIN: " + woogle.getCompString());
        // Log.d("AutoComplete", "HANZI: " + suggestions);


        return new Result(suggestions);
    }


}
