package de.viadee.sonarIssueScoring.service.prediction.load;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.springframework.stereotype.Component;

/**
 * Source for a TreeFilter, to only output interesting (java) files
 */
@Component
class TreeFilterSource {
    TreeFilter getTreeFilter() {
        return AndTreeFilter.create(PathSuffixFilter.create(".java"), new ExcludeTestRootsFilter());
    }

    /**
     * Tries to exclude test roots from the listings.
     * <p>
     * Known limitation: excludes any directory ending with *test. This includes directories like libtest.
     * It also isn't searching for source roots, even a package named test will be excluded.
     * <p>
     * Additionally, package-info and module-info are excluded, as they aren't "normal" too.     *
     */
    private static class ExcludeTestRootsFilter extends TreeFilter {
        private static final byte[] TEST = Constants.encode("test");
        private static final byte[] PACKAGE_INFO = Constants.encode("package-info.java");
        private static final byte[] MODULE_INFO = Constants.encode("module-info.java");

        @Override
        public boolean include(TreeWalk walker) {
            return !walker.isPathSuffix(TEST, TEST.length) && !walker.isPathSuffix(PACKAGE_INFO, PACKAGE_INFO.length) && !walker.isPathSuffix(MODULE_INFO,
                    MODULE_INFO.length);
        }

        @Override
        public boolean shouldBeRecursive() { return true;}

        //This is not an actual clone method, just named as such by the implemented interface
        @SuppressWarnings({"CloneInNonCloneableClass", "UseOfClone", "squid:S1182", "squid:S2975"})
        @Override
        public TreeFilter clone() { return this;}
    }
}
