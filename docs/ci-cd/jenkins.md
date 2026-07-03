# Jenkins CI/CD

Jenkins URL:

```text
http://110.42.239.130:8082/Jenkins
```

Anonymous access currently returns `403 Forbidden`, so the UI/API configuration must be completed with a Jenkins account that can create jobs and credentials.

## Pipeline Behavior

`Jenkinsfile` builds the GitHub repository on `main`:

1. Print Java, Maven, and Git versions.
2. Run backend tests with `mvn -B test`.
3. Package the backend jar with `mvn -B -DskipTests package`.
4. Archive the jar and deployment files.
5. Deploy the backend to `110.42.239.130` over SSH when `DEPLOY_BACKEND=true`.
6. Restart the `onboarding-api` systemd service and check `http://127.0.0.1:8080/api/health`.

`DEPLOY_BACKEND` defaults to `false` so the first Jenkins run can validate CI even before the production SSH credential is created. Enable it manually once `onboarded-prod-ssh` exists and the server env file has real values.

## Jenkins Requirements

Install or verify these Jenkins capabilities:

- Git plugin.
- Pipeline plugin.
- Credentials Binding plugin.
- JDK 21 or newer available to the Jenkins agent.
- Maven 3.9 or newer available to the Jenkins agent.

The pipeline uses `java` and `mvn` from the agent `PATH`, so make sure the Jenkins service environment points to JDK 21+.

## Credentials

Create this Jenkins credential:

```text
Kind: SSH Username with private key
ID: onboarded-prod-ssh
Username: <deployment ssh user>
Private Key: <private key that can SSH to 110.42.239.130 and run sudo>
```

The SSH user must be able to run `sudo` for deployment setup and service restart. The pipeline uses it for:

```bash
sudo mkdir -p /opt/onboarded/onboarding-api/incoming
sudo bash /opt/onboarded/onboarding-api/incoming/remote-deploy-backend.sh
```

If the Jenkins SSH user is not `root`, make sure it has passwordless sudo for these deployment commands.

## Server Bootstrap

On the deployment server, install Java 21 and create the production env file:

```bash
sudo mkdir -p /opt/onboarded/onboarding-api/incoming
sudo chown -R <deployment ssh user> /opt/onboarded/onboarding-api/incoming
sudo vi /opt/onboarded/onboarding-api/onboarding-api.env
sudo chmod 600 /opt/onboarded/onboarding-api/onboarding-api.env
```

Fill real MySQL, Redis, WeChat, and GLM values in:

```text
/opt/onboarded/onboarding-api/onboarding-api.env
```

Do not commit real secrets to GitHub.

## Jenkins Job Setup

Create a Pipeline job, for example:

```text
Onboarded-main
```

Use Pipeline from SCM:

```text
SCM: Git
Repository URL: https://github.com/chenguang-jiang/Onboarded.git
Branch: */main
Script Path: Jenkinsfile
```

For automatic builds, configure the GitHub webhook:

```text
http://110.42.239.130:8082/Jenkins/github-webhook/
```

If the Jenkins server is not reachable from GitHub, use polling as a fallback:

```text
H/2 * * * *
```

## First Deployment

The first deployment will create this example env file if it is missing and stop with a clear error:

```text
/opt/onboarded/onboarding-api/onboarding-api.env
```

Fill the production values, rerun the Jenkins build, and the pipeline will install or restart:

```text
onboarding-api.service
```
