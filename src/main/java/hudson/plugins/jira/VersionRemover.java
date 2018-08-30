package hudson.plugins.jira;

import com.atlassian.jira.rest.client.api.domain.Version;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Performs an action which removes a jira version, optionally moving issues to another version (version merging)
 */
public class VersionRemover {

    private static final Logger LOGGER = Logger.getLogger(VersionRemover.class.getName());

    protected boolean perform(Job<?, ?> project,
            String jiraProjectKey,
            String jiraVersion,
            String jiraMoveFixIssuesToVersion,
            String jiraMoveAffectedIssuesToVersion,
            Run<?, ?> run,
            TaskListener listener) {
        String realProjectKey = "NOT_SET";
        String realVersion = "NOT_SET";
        String realMoveFixIssuesToVersion = "NOT_SET";
        String realMoveAffectedIssuesToVersion = "NOT_SET";

        try {
            realProjectKey = run.getEnvironment(listener).expand(jiraProjectKey);
            realVersion = run.getEnvironment(listener).expand(jiraVersion);
            realMoveFixIssuesToVersion = run.getEnvironment(listener).expand(jiraMoveFixIssuesToVersion);
            realMoveAffectedIssuesToVersion = run.getEnvironment(listener).expand(jiraMoveAffectedIssuesToVersion);

            if (StringUtils.isEmpty(realProjectKey)) {
                throw new IllegalArgumentException("No project specified");
            }

            if (StringUtils.isEmpty(realVersion)) {
                throw new IllegalArgumentException("Version is Empty");
            }

            JiraSite site = getSiteForProject(project);
            Set<Version> versions = site.getVersions(realProjectKey);

            final String finalVersion = realVersion;
            Optional<Version> matchingVersion = versions.stream()
                    .filter(version -> version.getName().equals(finalVersion)).findFirst();

            final String finalMoveFixIssuesToVersion = realMoveFixIssuesToVersion;
            Optional<Version> matchingMoveFixIssuesToVersion = versions.stream()
                    .filter(version -> version.getName().equals(finalMoveFixIssuesToVersion)).findFirst();

            final String finalMoveAffectedIssuesToVersion = realMoveAffectedIssuesToVersion;
            Optional<Version> matchingMoveAffectedIssuesToVersion = versions.stream()
                    .filter(version -> version.getName().equals(finalMoveAffectedIssuesToVersion)).findFirst();

            if (matchingVersion.isPresent()) {
                listener.getLogger().println(Messages.VersionReleaser_RemovingVersion(realVersion, realProjectKey));
                removeVersion(realProjectKey, matchingVersion.get(), matchingMoveFixIssuesToVersion.orElse(null), matchingMoveAffectedIssuesToVersion.orElse(null), site.getSession());
            } else {
                listener.getLogger().println(Messages.VersionRemover_VersionNotFound(realVersion, realProjectKey));
            }
        } catch (Exception e) {
            e.printStackTrace(
                    listener.fatalError("Unable to remove version %s to JIRA project %s", realVersion, realProjectKey, e));

            if (listener instanceof BuildListener) {
                ((BuildListener) listener).finished(Result.FAILURE);
            }
            return false;
        }
        return true;
    }

    protected void removeVersion(String projectKey, Version version, @Nullable Version moveFixIssuesToVersion, @Nullable Version moveAffectedIssuesToVersion, JiraSession session) {
        if (session == null) {
            LOGGER.warning("JIRA session could not be established");
            return;
        }

        session.removeVersion(projectKey, version, moveFixIssuesToVersion, moveAffectedIssuesToVersion);
    }

    protected JiraSite getSiteForProject(Job<?, ?> project) {
        return JiraSite.get(project);
    }

}
