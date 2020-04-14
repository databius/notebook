package com.databius.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

public class MapElementsExample {

    public static void main(String[] args) {
        System.out.println("Main class for DirectRunner");

        // Pipeline create using default runner (DirectRunnter)
        // Interface: PipelineOptions
        PipelineOptions options = PipelineOptionsFactory.create();

        Pipeline p = Pipeline.create(options);

        // Example pcollection
        final List<String> LINES = Arrays.asList(
                "blah"
        );
        // via ProcessFunction
        PCollection<String> p0 = p.apply(Create.of(LINES));

//        p0.apply(ParDo.of(new PrintResultsFn2()));

        // via ProcessFunction
        PCollection p1 = p.apply(Create.of(LINES))
                .apply(MapElements.into(TypeDescriptors.integers())
                        .via((String word) -> word.length()))
                .apply(MapElements.into(TypeDescriptors.integers())
                        .via((Integer word) -> word * 2))
                .apply(ParDo.of(new PrintResultsFn2()));

// via in-line SimpleFunction
        PCollection p2 = p.apply(Create.of(LINES))
                .apply(MapElements.via(new SimpleFunction<String, List<String>>() {
                    public List<String> apply(String word) {
                        return Arrays.asList(word, "Its weekend!");
                    }
                }))
                .apply(ParDo.of(new PrintResultsFn()));

// via AddFieldFn class
        PCollection p3 = p.apply(Create.of(LINES))
                .apply(MapElements.via(new AddFieldFn()))
                .apply(ParDo.of(new PrintResultsFn()));

        // Read lines from file
//        p.apply(Create.of(LINES))
//                .apply(MapElements.via(new AddFieldFn()))
//                .apply(TextIO.write().to("/tmp/test-out"));

        p.run().waitUntilFinish();
    }
}

// define AddFieldFn extending from SimpleFunction and overriding apply method
class AddFieldFn extends SimpleFunction<String, List<String>> {
    @Override
    public List<String> apply(String word) {
        return Arrays.asList(word, "Its weekend!");
    }
}

// just print the results
class PrintResultsFn extends DoFn<List<String>, Void> {
    @DoFn.ProcessElement
    public void processElement(@Element List<String> words) {
        System.out.println(Arrays.toString(words.toArray()));
    }
}

// just print the results
class PrintResultsFn2 extends DoFn<Integer, Void> {
    @DoFn.ProcessElement
    public void processElement(@Element Integer words) {
        System.out.println(words);
    }
}