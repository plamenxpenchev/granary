#!/bin/bash

set -e
set -o pipefail

if [ "$#" -ne 1 ]; then
    echo "Expected 1 parameter to script: the path to the age secret key file (SOPS_AGE_KEY_FILE)."
    exit 1
fi

GRANARY_ROOT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)

SOPS_ENV_FILE=${GRANARY_ROOT_DIR}/.sops.env
SOPS_GRANARY_PRS_FILE=${GRANARY_ROOT_DIR}/properties/src/main/resources/sops.granary.prs

if [ ! -f ${SOPS_ENV_FILE} ]; then
    echo "${SOPS_ENV_FILE} not found."
    exit 1
fi

if [ ! -f ${SOPS_GRANARY_PRS_FILE} ]; then
    echo "${SOPS_GRANARY_PRS_FILE} not found."
    exit 1
fi

ENV_FILE=${GRANARY_ROOT_DIR}/.env
GRANARY_PRS_FILE=${GRANARY_ROOT_DIR}/properties/src/main/resources/granary.prs

export SOPS_AGE_KEY_FILE=$1

sops decrypt --input-type dotenv --output-type dotenv ${SOPS_ENV_FILE} > ${ENV_FILE}
sops decrypt --input-type dotenv --output-type dotenv ${SOPS_GRANARY_PRS_FILE} > ${GRANARY_PRS_FILE}

cd ${GRANARY_ROOT_DIR}
docker compose up
