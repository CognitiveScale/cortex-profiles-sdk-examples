#!/bin/bash -eux

function build() {
  apt update &> /dev/null
  apt install -q -y make &> /dev/null
  make build
}

function buildLocal() {
  make build
}

### MAIN ####
VERSION=$(git describe --long --always --match='v*.*' | sed 's/v//; s/-/./')
echo "##### BUILDING ${VERSION} ######"
case ${1-local} in
 CI*)
  build
  ;;
*)

 buildLocal
 ;;
esac
