#!/usr/bin/env bash
set -euo pipefail

git config core.hooksPath .githooks
echo "Git hooks installed for this repo."
echo "To uninstall: git config --unset core.hooksPath"
