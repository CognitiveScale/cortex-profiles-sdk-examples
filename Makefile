.PHONY: clean build build-daemon create-app-image create-daemon-image test start-daemon deploy-daemon
-include .build_file

TAG := latest
DOCKER_IMAGE := profiles-example
DAEMON_IMAGE := pdaemon
ifndef DAEMON_TAG
	curr_hash:=$(shell git rev-parse --short HEAD)
	ifneq ($(curr_hash), $(GIT_HASH))
		GIT_HASH=$(curr_hash)

		SKILL_VERSION=-1
	else
		ifndef SKILL_VERSION
			SKILL_VERSION=-1
		endif
	endif
	SKILL_VERSION:=$(shell expr $(SKILL_VERSION) + 1)
	dummy1:=$(shell echo "GIT_HASH=$(shell git rev-parse --short HEAD)" >".build_file")
	dummy:=$(shell echo "SKILL_VERSION=$(SKILL_VERSION)">> ".build_file")
endif
DAEMON_TAG?=$(GIT_HASH)-$(SKILL_VERSION)
URL=$(shell cat ~/.cortex/config | jq .profiles[.currentProfile].url)
ifndef DOCKER_PREGISTRY_URL
	DOCKER_PREGISTRY_URL=$(shell curl ${URL}/fabric/v4/info | jq -r .endpoints.registry.url | sed 's~http[s]*://~~g')
endif
SPARK_CONTAINER := ${DOCKER_PREGISTRY_URL}/${DOCKER_IMAGE}:${TAG}
DAEMON_CONTAINER := ${DOCKER_PREGISTRY_URL}/${DAEMON_IMAGE}:${DAEMON_TAG}

all: clean build create-app-image deploy-skill

create-app-image:
	docker build --build-arg base_img=c12e/spark-template:profile-jar-base-6.3.1 -t ${DOCKER_IMAGE}:${TAG} -f ./main-app/build/resources/main/Dockerfile ./main-app/build

create-daemon-image:
	docker build --no-cache -t ${DAEMON_IMAGE}:${DAEMON_TAG}  -f ./profiles-daemon/Dockerfile .

# Build the Application
build:
	./gradlew build

build-daemon:
	./gradlew :profile-daemon:build

# Test the Application
test:
	./gradlew test

clean:
	./gradlew clean

# Start the Daemon locally
start-daemon:
	./gradlew :profile-daemon:bootRun


# Tag the latest create-app-image built container
tag-container: check-env
	docker tag ${DOCKER_IMAGE}:${TAG} ${SPARK_CONTAINER}

# Tag the latest create-daemon-image built container
tag-daemon-container: check-env
	docker tag ${DAEMON_IMAGE}:${DAEMON_TAG} ${DAEMON_CONTAINER}

# Push the container
push-container: check-env
	docker push ${SPARK_CONTAINER}

push-daemon-container: check-env
	docker push ${DAEMON_CONTAINER}

# Deploy the action and save the skill
skill-save: check-env
	cortex actions deploy --actionName ${DOCKER_IMAGE} --actionType job --docker ${SPARK_CONTAINER} --project ${PROJECT_NAME} --cmd '["scuttle", "python", "submit_job.py"]' --podspec ./templates/podspec.yaml
	cortex skills save -y templates/skill.yaml --project ${PROJECT_NAME}

daemon-skill-save: check-env
	cortex actions deploy --actionName ${DAEMON_IMAGE} --actionType daemon --docker ${DAEMON_CONTAINER} --project ${PROJECT_NAME} --port '8080' --environmentVariables '"REDIS_HOST"="localhost","REDIS_PORT"="6379","REDIS_USER"="default","REDIS_PASSWORD"=""'
	cortex skills save -y profiles-daemon/skill.yaml --project ${PROJECT_NAME}

# Save types
types-save: check-env
	cortex types save -y templates/types.yaml --project ${PROJECT_NAME}

# Building and pushing the skill container, saving the skill and the types
deploy-skill: tag-container push-container types-save skill-save

# Building and pushing the daemon skill container, saving the skill and the action
deploy-daemon: build-daemon create-daemon-image tag-daemon-container push-daemon-container daemon-skill-save

# Invoke the skill with payload
invoke: check-env
	cortex skills invoke --params-file templates/payload.json profiles-example params --project ${PROJECT_NAME}

check-env:
ifndef DOCKER_PREGISTRY_URL
	$(error environment variable DOCKER_PREGISTRY_URL is not set. Set it like <docker-registry-url>/<namespace-org>)
endif
ifndef PROJECT_NAME
	$(error environment variable PROJECT_NAME is not set. Set this to Cortex project name.)
endif
