package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.hash.Hashing;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;

/**
 * Extracts the predicitons target variable out ot the repository
 */
@Service

public class TargetExtractor {

    @VisibleForTesting
    static Multiset<Path> createChangeHistogram(List<Commit> commits) {
        return commits.stream().flatMap(c -> c.diffs().keySet().stream()).collect(ImmutableMultiset.toImmutableMultiset());
    }

    public void extractTargetVariable(Repo future, ImmutableMap<Path, Builder> output) {
        Multiset<Path> changeHistogram = createChangeHistogram(future.commits());
        //Basically the same data as above, but this map does explicitly contain 0, making it useable for median calculations
        ImmutableMap<Path, Integer> pathCounts = output.keySet().stream().collect(ImmutableMap.toImmutableMap((Path p) -> p, changeHistogram::count));

        Map<Path, Double> ranks = rank(pathCounts);

        output.forEach((path, out) -> {
            //Hashing is used to make the assignment static
            int split = new Random(Hashing.murmur3_128().hashUnencodedChars(path.toString()).asLong()).nextInt(10) - 2;
            // -2: Leader, -1: Validation, 0..9/ => Training
            out.fold(split > 0 ? split / 2 : split);

            out.targetEditCountPercentile(ranks.get(path));
        });
    }

    public void fillDummyTargetValues(Map<Path, Builder> output) {
        output.forEach((path, out) -> out.fold(0).targetEditCountPercentile(0));
    }

    @VisibleForTesting
    static Map<Path, Double> rank(ImmutableMap<Path, Integer> in) {
        Path[] paths = in.keySet().toArray(new Path[0]);

        PercentilesScaledRanking ranking = new PercentilesScaledRanking(TiesStrategy.MINIMUM);
        double[] ranks = ranking.rank(in.values().stream().mapToDouble(i -> i).toArray());

        return IntStream.range(0, paths.length).boxed().collect(ImmutableMap.toImmutableMap(i -> paths[i], i -> ranks[i]));
    }

    //https://stackoverflow.com/a/20908082
    private static class PercentilesScaledRanking extends NaturalRanking {

        PercentilesScaledRanking(TiesStrategy tiesStrategy) {
            super(NaNStrategy.FAILED, tiesStrategy);
        }

        @Override
        public double[] rank(double[] data) {
            double[] rank = super.rank(data);
            for (int i = 0; i < rank.length; i++)
                rank[i] = (rank[i] - 1) / rank.length;
            return rank;
        }
    }
}
