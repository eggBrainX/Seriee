#!/usr/bin/env bash
#
# Applies the repository settings described in SECURITY.md:
#
#   1. Actions may only use GitHub-owned or verified Marketplace actions.
#   2. The default GITHUB_TOKEN is read-only.
#   3. `main` requires a pull request and the CI check; no force-push/delete.
#   4. `v*` tags cannot be updated or deleted.
#   5. The `release` environment requires your approval and only accepts
#      deployments from `v*` tags, so signing secrets cannot be reached from a
#      branch push.
#
# Requirements: gh (authenticated with admin rights on the repository).
#
# Usage:
#   ./scripts/harden-repo.sh              # apply
#   DRY_RUN=1 ./scripts/harden-repo.sh    # print the calls, change nothing
#
# Overridable: REPO, BRANCH, CI_CHECK, ENV_NAME, TAG_PATTERN.
set -euo pipefail

REPO="${REPO:-$(gh repo view --json nameWithOwner --jq .nameWithOwner)}"
BRANCH="${BRANCH:-main}"
CI_CHECK="${CI_CHECK:-Lint, test, and build}"
ENV_NAME="${ENV_NAME:-release}"
TAG_PATTERN="${TAG_PATTERN:-v*}"
DRY_RUN="${DRY_RUN:-0}"

log() { printf '\n==> %s\n' "$1"; }

run() {
  if [ "$DRY_RUN" = "1" ]; then
    printf '  (dry-run) %s\n' "$*"
  else
    "$@"
  fi
}

log "Repository: $REPO"

# --- 1 & 2: Actions policy -------------------------------------------------
log "Restricting GitHub Actions to GitHub-owned and verified actions"
run gh api -X PUT "repos/$REPO/actions/permissions" \
  -F enabled=true -f allowed_actions=selected
# gradle/actions is allowlisted explicitly so the build keeps working even if
# the Marketplace verification flag changes.
run gh api -X PUT "repos/$REPO/actions/permissions/selected-actions" \
  -F github_owned_allowed=true -F verified_allowed=true \
  -f 'patterns_allowed[]=gradle/actions@*'

log "Making the default GITHUB_TOKEN read-only"
run gh api -X PUT "repos/$REPO/actions/permissions/workflow" \
  -f default_workflow_permissions=read \
  -F can_approve_pull_request_reviews=false

# --- 3: Branch protection --------------------------------------------------
# required_approving_review_count is 0 so a solo maintainer can still merge;
# raise it to 1 once there is a second reviewer.
log "Protecting $BRANCH (pull request + CI check, no force-push, no deletion)"
run gh api -X PUT "repos/$REPO/branches/$BRANCH/protection" --input - <<JSON
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["$CI_CHECK"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "required_approving_review_count": 0,
    "dismiss_stale_reviews": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON

# --- 4: Tag protection ----------------------------------------------------
# `stable-latest` is intentionally not covered: the release workflow moves it.
log "Protecting $TAG_PATTERN tags from update and deletion"
if gh api "repos/$REPO/rulesets" --jq '.[].name' 2>/dev/null | grep -qx 'Protect release tags'; then
  echo "  ruleset already exists"
else
  run gh api -X POST "repos/$REPO/rulesets" --input - <<JSON
{
  "name": "Protect release tags",
  "target": "tag",
  "enforcement": "active",
  "conditions": {
    "ref_name": {
      "include": ["refs/tags/$TAG_PATTERN"],
      "exclude": []
    }
  },
  "rules": [
    { "type": "deletion" },
    { "type": "update" }
  ]
}
JSON
fi

# --- 5: Release environment -----------------------------------------------
# Create this environment BEFORE pushing the first v* tag: a job that
# references a non-existent environment would otherwise create it unprotected.
log "Creating the '$ENV_NAME' environment with you as required reviewer"
ADMIN_ID=$(gh api user --jq .id)
run gh api -X PUT "repos/$REPO/environments/$ENV_NAME" --input - <<JSON
{
  "reviewers": [{ "type": "User", "id": $ADMIN_ID }],
  "prevent_self_review": false,
  "deployment_branch_policy": {
    "protected_branches": false,
    "custom_branch_policies": true
  }
}
JSON

log "Allowing only $TAG_PATTERN tags to deploy to '$ENV_NAME'"
existing=$(
  gh api "repos/$REPO/environments/$ENV_NAME/deployment-branch-policies" \
    --jq '.branch_policies[].name' 2>/dev/null || true
)
if printf '%s\n' "$existing" | grep -qx "$TAG_PATTERN"; then
  echo "  policy already exists"
else
  run gh api -X POST "repos/$REPO/environments/$ENV_NAME/deployment-branch-policies" \
    -f name="$TAG_PATTERN" -f type=tag
fi

cat <<'TXT'

Done. Two things this script cannot do through the public API:

  1. Settings -> Actions -> General -> Fork pull request workflows:
     choose "Require approval for all outside collaborators".
  2. Store the signing secrets on the environment, not the repository:

       gh secret set ANDROID_KEYSTORE_BASE64 --env release < seriee-release.keystore.b64
       gh secret set ANDROID_KEYSTORE_PASSWORD --env release
       gh secret set ANDROID_KEY_ALIAS --env release
       gh secret set ANDROID_KEY_PASSWORD --env release

     (base64 -w0 seriee-release.keystore > seriee-release.keystore.b64)
TXT
