package com.fongmi.android.tv.player.extractor;

import android.net.Uri;

import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.Task;

import java.util.ArrayList;
import java.util.List;

public class Source {

    private final List<Extractor> extractors;

    public Source() {
        extractors = new ArrayList<>();
        extractors.add(new Push());
        extractors.add(new Strm());
        extractors.add(new Video());
    }

    public static Source get() {
        return Loader.INSTANCE;
    }

    private Extractor getExtractor(Uri uri) {
        return extractors.stream().filter(extractor -> extractor.match(uri)).findFirst().orElse(null);
    }

    public void parse(Vod vod) throws Exception {
    }

    public String fetch(Result result) throws Exception {
        Uri uri = result.getUrl().uri();
        String url = result.getUrl().v();
        Extractor extractor = getExtractor(uri);
        if (extractor != null) result.setParse(0);
        if (extractor instanceof Video) result.setParse(1);
        return extractor == null ? url : extractor.fetch(result);
    }

    public void stop() {
        if (extractors == null) return;
        extractors.forEach(Extractor::stop);
    }

    public void exit() {
        if (extractors == null) return;
        Task.execute(() -> extractors.forEach(Extractor::exit));
    }

    public interface Extractor {

        default String fetch(Result result) throws Exception {
            return fetch(result.getUrl().v());
        }

        String fetch(String url) throws Exception;

        boolean match(Uri uri);

        void stop();

        void exit();
    }

    private static class Loader {
        static volatile Source INSTANCE = new Source();
    }
}
