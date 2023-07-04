package nl.ing.lovebird.cassandra.test;

@SuppressWarnings("squid:S1118")
class CIConditions {

    static boolean isRunningInGitlabCI() {
        return System.getenv("GITLAB_CI") != null;
    }
}
