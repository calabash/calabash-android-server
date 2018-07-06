#!/usr/bin/env bash

set -e

source bin/log.sh

BRANCH=$(git rev-parse --symbolic-full-name --abbrev-ref HEAD | tr -d "\n")

if [ "${BRANCH}" != "master" ]; then
  error "Only create tags from the master branch"
  error "The current branch is: ${BRANCH}"
  exit 1
fi

TAG=$(cat version | tr -d "\n")

git tag -a "${TAG}" -m"${TAG}"
git push origin "${TAG}"
git branch "tag/${TAG}" "${TAG}"
git checkout "tag/${TAG}"
