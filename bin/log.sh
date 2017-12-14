# Log functions for scripts.
#
# Suitable for Xcode Run Script Build Phase and command line scripts.
#
# Usage:
#
# source bin/log.sh

function info {
  if [ "${TERM}" = "dumb" ]; then
    echo -e "INFO: $1"
  else
    echo -e "$(tput setaf 2)INFO: $1$(tput sgr0)"
  fi
}

function shell {
  if [ "${TERM}" = "dumb" ]; then
    echo -e "SHELL: $1"
  else
    echo -e "$(tput setaf 6)SHELL: $1$(tput sgr0)"
  fi
}

function error {
  if [ "${TERM}" = "dumb" ]; then
    echo -e "ERROR: $1"
  else
    echo -e "$(tput setaf 1)ERROR: $1$(tput sgr0)"
  fi
}

function banner {
  if [ "${TERM}" = "dumb" ]; then
    echo ""
    echo "######## $1 ########"
    echo ""
  else
    echo ""
    echo "$(tput setaf 5)######## $1 ########$(tput sgr0)"
    echo ""
  fi
}

