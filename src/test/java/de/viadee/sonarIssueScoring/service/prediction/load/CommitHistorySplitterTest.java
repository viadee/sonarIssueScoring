package de.viadee.sonarIssueScoring.service.prediction.load;

import java.time.DayOfWeek;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class CommitHistorySplitterTest {
    @Test
    public void testSplitting() throws Exception {
        ImmutableList<Commit> commits = ImmutableList.of( //
                Commit.of("ignored-at-start", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //0
                Commit.of("ignored-at-start", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //1
                Commit.of("future", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //2
                Commit.of("future", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //3
                Commit.of("future", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //4
                Commit.of("past", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //5
                Commit.of("past", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //6
                Commit.of("past", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()), //7
                Commit.of("past", "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of()));//8

        RepositorySnapshotCreator snapshotCreator = Mockito.mock(RepositorySnapshotCreator.class);
        Mockito.when(snapshotCreator.createSnapshot(Mockito.any(), Mockito.any())).thenReturn(ImmutableMap.of());

        CommitHistorySplitter splitter = new CommitHistorySplitter(snapshotCreator);

        PastFuturePair pair = splitter.splitCommits(null, commits, 2, 3);

        Assert.assertEquals(3, pair.future().commits().size());
        Assert.assertEquals(4, pair.past().commits().size());

        for (Commit commit : pair.past().commits())
            Assert.assertEquals("past", commit.id());

        for (Commit commit : pair.future().commits())
            Assert.assertEquals("future", commit.id());
    }
}