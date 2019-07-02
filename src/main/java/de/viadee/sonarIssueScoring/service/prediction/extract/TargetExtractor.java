package de.viadee.sonarIssueScoring.service.prediction.extract;

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
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

/**
 * Extracts the prediction target variable out ot the repository
 */
@Service
@SuppressWarnings("squid:S2245") // Random numbers are safe here
public class TargetExtractor {

    public void extractTrainingHelpers(Commit c, Output out) {
        c.content().keySet().forEach(path -> out.add(c, path, Instance.NAME_RANDOM, Math.random()));
        c.content().keySet().forEach(path -> out.add(c, path, Instance.NAME_FOLD, fold(path)));
    }

    private int fold(GitPath p) {
        //Hashing is used to make the assignment static
        int split = new Random(p.hashCode()).nextInt(10) - 2;
        return split > 0 ? split / 2 : split;
    }

    public void extractTargetVariable(Commit base, List<Commit> futureCommits, Output out) {
        Multiset<GitPath> changeHistogram = createChangeHistogram(futureCommits);
        //Basically the same data as above, but this map does explicitly contain 0, making it useable for median calculations
        Map<GitPath, Integer> pathCounts = Maps.toMap(base.content().keySet(), changeHistogram::count);

        Map<GitPath, Double> ranks = rank(pathCounts);

        base.content().keySet().forEach(path -> out.add(base, path, Instance.NAME_TARGET, ranks.get(path)));
    }

    @VisibleForTesting static Multiset<GitPath> createChangeHistogram(List<Commit> commits) {
        return commits.stream().flatMap(c -> c.diffs().keySet().stream()).collect(ImmutableMultiset.toImmutableMultiset());
    }

    @VisibleForTesting static Map<GitPath, Double> rank(Map<GitPath, Integer> in) {
        GitPath[] paths = in.keySet().toArray(new GitPath[0]);

        PercentilesScaledRanking ranking = new PercentilesScaledRanking();
        double[] ranks = ranking.rank(in.values().stream().mapToDouble(i -> i).toArray());

        return IntStream.range(0, paths.length).boxed().collect(ImmutableMap.toImmutableMap(i -> paths[i], i -> ranks[i]));
    }

    //https://stackoverflow.com/a/20908082
    private static class PercentilesScaledRanking extends NaturalRanking {

        PercentilesScaledRanking() {
            super(NaNStrategy.FAILED, TiesStrategy.MINIMUM);
        }

        @Override public double[] rank(double[] data) {
            if (data.length == 0) //Can happen if a commit removes every java-file in the repository
                return new double[0];

            double[] rank = super.rank(data);
            for (int i = 0; i < rank.length; i++)
                rank[i] = (rank[i] - 1) / rank.length;
            return rank;
        }
    }
}
