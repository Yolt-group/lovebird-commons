package com.yolt.teamstarter;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import io.fabric8.kubernetes.client.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import static io.fabric8.kubernetes.client.Config.KUBERNETES_KUBECONFIG_FILE;

/**
 * {@link io.fabric8.kubernetes.client.KubernetesClient} (io.fabric8:kubernetes-client:4.13.3) silently picks up
 * current k8s context from ~/.kube/context file if it can't find provided context.
 * Since we might affect other environments with it's behaviour, first we need to check that required context exists in
 * local k8s config and only then proceed with operations.
 */
class KubernetesContextValidator {

    private KubernetesContextValidator(){

    }

    static void validate(final String contextName) {
        Objects.requireNonNull(contextName, "Context name should not be null");

        final File k8sConfigFile = new File(Utils.getSystemPropertyOrEnvVar(
                KUBERNETES_KUBECONFIG_FILE, new File(getHomeDir(), ".kube" + File.separator + "config").toString()));
        try {
            final Config k8sModelConfig = KubeConfigUtils.parseConfig(k8sConfigFile);

            // context is specified in 'current-context' property of k8s configuration
            // we need to override that in order to check whether requested context would be picked up
            k8sModelConfig.setCurrentContext(contextName);

            final NamedContext currentContext = KubeConfigUtils.getCurrentContext(k8sModelConfig);
            if (currentContext == null) {
                throw new IllegalStateException(String.format("Configuration for context '%s' does not exist, you should first run 'vault-helper k8s -cluster %s'",
                        contextName, contextName.split("\\.")[1]));
            }

            if (!contextName.equals(currentContext.getContext().getCluster())) {
                throw new IllegalStateException(String.format("Actual context is '%s' while requested was '%s', this should not normally happen, however, can't proceed forward, please contact platform-team",
                        currentContext.getContext().getCluster(), contextName));
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("File '%s' is either not present or not parseable", k8sConfigFile.getAbsolutePath()), e);
        }
    }

    /**
     * Taken from {@link io.fabric8.kubernetes.client.Config} class.
     *
     * @return home directory (presumably where .kube folder with k8s configuration is located)
     */
    @SuppressWarnings("squid:S3776") // we want to maintain a copy of a 3rd-party code exactly as it is
    private static String getHomeDir() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.startsWith("win")) {
            String homeDrive = System.getenv("HOMEDRIVE");
            String homePath = System.getenv("HOMEPATH");
            if (homeDrive != null && !homeDrive.isEmpty() && homePath != null && !homePath.isEmpty()) {
                String homeDir = homeDrive + homePath;
                File f = new File(homeDir);
                if (f.exists() && f.isDirectory()) {
                    return homeDir;
                }
            }
            String userProfile = System.getenv("USERPROFILE");
            if (userProfile != null && !userProfile.isEmpty()) {
                File f = new File(userProfile);
                if (f.exists() && f.isDirectory()) {
                    return userProfile;
                }
            }
        }
        String home = System.getenv("HOME");
        if (home != null && !home.isEmpty()) {
            File f = new File(home);
            if (f.exists() && f.isDirectory()) {
                return home;
            }
        }

        throw new IllegalStateException("Could not detect a home directory, please specify it via 'kubeconfig' property");
    }
}
