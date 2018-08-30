package hudson.plugins.jira;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class JiraVersionRemoverBuilder extends Builder implements SimpleBuildStep {

    private String jiraProjectKey;
    private String jiraVersion;
    private String jiraMoveFixIssuesToVersion;
    private String jiraMoveAffectedIssuesToVersion;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @DataBoundConstructor
    public JiraVersionRemoverBuilder(String jiraProjectKey, String jiraVersion, String jiraMoveFixIssuesToVersion, String jiraMoveAffectedIssuesToVersion) {
        this.jiraProjectKey = jiraProjectKey;
        this.jiraVersion = jiraVersion;
        this.jiraMoveFixIssuesToVersion = jiraMoveFixIssuesToVersion;
        this.jiraMoveAffectedIssuesToVersion = jiraMoveAffectedIssuesToVersion;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) {
        new VersionRemover().perform(run.getParent(), jiraProjectKey, jiraVersion, jiraMoveFixIssuesToVersion, jiraMoveAffectedIssuesToVersion, run, listener);
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private DescriptorImpl() {
            super(JiraVersionRemoverBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            // Placed in the build settings section
            return Messages.JiraVersionRemoverBuilder_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/jira/help.html";
        }

        @Override
        public JiraVersionRemoverBuilder newInstance(StaplerRequest req, JSONObject formData)
                throws FormException {
            return req.bindJSON(JiraVersionRemoverBuilder.class, formData);
        }
    }
}
