# Readme.md

## kafka-demo-scripts

This folder contains scripts and supporting files to execute the solace ep kafka demo. This folder is incorporated into the Docker container.
- All of the script files depend on the existence of a valid configuration file specific to the target cluster.
- The configuration file must be specified using the environment variable **CONFIG_FILE**
- Most scripts require that the environment variables defined in env.config be set prior to execution. (auto-exec-demo.sh is an exception)

## Contents

### env.config
Sets generic environment variables on the shell to support the demo scripts. Usage: ```source env.config```

### create-topics.sh
Self-explanatory. This script will create a set of topics defined by file: **"topics.txt"**. The script will not fail if the topics in the list have already been defined.

### exec-demo.sh
Designed to execute the demo services from an interactive prompt. Assumes that the topics defined in **topics.txt** already exist.

### auto-exec-demo.sh
This is the default command of the kafka-demo image defined by the Dockerfile. This script will set the **env.config** and call **create-topics.sh** before executing the demo services.
This command is the entrypoint for the dockerfile and will be automatically executed in detached mode.

### kill-svc.sh
This script can be used to shut down the services associated with the demo.
Usage: ```kill-svc.sh .microservices.pids```. The **.microservices.pids** file is created by the **exec-demo.sh** and **auto-exec-demo.sh** scripts. 

### create-topics-ccloud.sh
This command is not used by the demo. It is intended to to be used with the Confluent CLI instead of the pure kafka CLI.
