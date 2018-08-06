package de.viadee.sonarIssueScoring.service.prediction.train;

/**
 * Represents an n-th last edit to a file
 * <p>
 * LastCommit is how many commits ago from the head a specific file was last changed
 */
public enum CommitAge {
    LastCommit(0),
    Minus1(1),
    Minus2(2),
    Minus4(4),
    Minus8(8),
    Minus16(16);

    private final int offset;

    CommitAge(int offset) {
        this.offset = offset;
    }

    public int offset() {return offset;}
}

