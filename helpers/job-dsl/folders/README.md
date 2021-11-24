# Jenkins Folders Helper
---
## Table of Contents

- [Description](#description)

- [Requirements](#requirements)

- [Usage](#usage)

---
## Description

**Folder helpers** provide code-based Jenkins folders definition. 

Jenkins folders and their permissions are defined in YAML files hosted in your code repository.

## Requirements

To use folder helper, some plugins needs to be installed:
- [job-dsl](https://plugins.jenkins.io/job-dsl/)
- [matrix-auth](https://plugins.jenkins.io/matrix-auth/)
- [folders](https://plugins.jenkins.io/cloudbees-folder/)
- [folder-properties](https://plugins.jenkins.io/folder-properties/)

Seed job-dsl needs to be [configured](https://www.digitalocean.com/community/tutorials/how-to-automate-jenkins-job-configuration-using-job-dsl).

Project-based Matrix Authorization Strategy needs to be configured in Jenkins "Configure Global Security"

A code repository to host configuration files

## Usage

Point your seed job to read the [folders.groovy](folders.groovy) file

<details><summary>Define main folder configuration</summary>

```

  config:
    main_folder: "services" # Name of the main folder.
    main_folder_display_name: "Services" # Name of the main folder which will be displayed in Jenkins UI.
    main_folder_description: "Services" # Name of the main folder which will be displayed in Jenkins UI.
    allow_all_auth_users: true # Allow all authenticated Jenkins users to view the main folder. 
    allowed_users:
      - allowed-user # List of Jenkins users allowed to view the main folder.
    allowed_groups:
      - allowed-group # List of Jenkins groups allowed to view the main folder.

```
</details>

<details><summary>Define your service folders in yaml files</summary>

```

  example-service: #name of the service folder which will be displayed under the main folder.
    owner: user-owner # Name of the user who owns the service (optional).
    group_owner: group-owner # # Name of the user who owns the service (optional).
    allowed_users: # List of additional users with full permissions.
      - allowed-user
    allowed_ro_users: # List of additional users with read permissions.
      - allowed-ro-user
    allowed_groups: # List of additional groups with full permissions.
      - allowed-group
    folder_properties: # Folder properties that will be added to your service folders.
      SAMPLE_PROPERTY: 'some value'

```
</details>

Folder helper tries to find configuration files in helpers/job-dsl/folders/files/${JENKINS_SERVER_NAME}.

This value can be overridden by setting **folders_config_path** environment variable in Jenkins global parameters.
