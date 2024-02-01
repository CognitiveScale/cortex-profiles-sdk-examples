#!/bin/bash -eux

function build() {
  apt update &> /dev/null
  apt install -q -y make &> /dev/null
  VERSION=${BUILD_VERSION} make build sonar
}

function buildLocal() {
  VERSION=${BUILD_VERSION} make build
}

### MAIN ####
git config --global --add safe.directory /work
BUILD_VERSION="$(git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')"

if [ -z "${BUILD_VERSION}" ];then
  BUILD_VERSION="1.0.0-SNAPSHOT"
fi

echo "##### BUILDING ${BUILD_VERSION} ######"
case ${1-local} in
 CI*)
  build
  ;;
*)

 buildLocal
 ;;
esac
