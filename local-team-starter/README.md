# Local team starter

Connect from your local environment to a team environment. Basically it downloads the secrets from K8s cluster and 
picks up the values from the K8s config map.

Need more? Add an extra configuration which can use the K8sClient to get the configuration from the environment.

## Usage

### Step 1: Configure K8s locally

- Run https://git.yolt.io/backend-tools/developer-toolbox/-/blob/master/scripts/vault-helper-all.sh to connect to K8s

### Step 2: Add new property file

In your project create a new property file, for example: `application-team10.yml` In this file add the following:

```
yolt.team.starter:
  kubernetes-context: team1
  environment: team1
  namespace: default
  application-name: ${spring.application.name}
  container-name: ${yolt.team.starter.application-name} # Use if the pod consists of multiple containers like api-gw and security-server

```

NOTE: Please update the `management.server.ssl.enabled` to `false` if needed.

Start your application with this new Spring profile and activate the Maven profile called `local-team-starter`. The 
profile will add the correct dependencies to the project.
