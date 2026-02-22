#!/usr/bin/env bash
set -euo pipefail

# set_railway_env.sh
# Usage:
#   ./set_railway_env.sh --private-domain <domain> --tcp-domain <tcp_domain> --tcp-port <tcp_port>
# Or run without args to be prompted.

show_usage(){
  cat <<EOF
Usage: $0 [--private-domain DOMAIN] [--tcp-domain DOMAIN] [--tcp-port PORT]

This script logs into Railway (interactive if needed), links the current folder to a Railway project (if not already linked),
and sets the MySQL environment variables required by the application.

It will mark sensitive values as secrets where supported.
EOF
}

PRIVATE_DOMAIN=""
TCP_DOMAIN=""
TCP_PORT=""

# parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --private-domain) PRIVATE_DOMAIN="$2"; shift 2;;
    --tcp-domain) TCP_DOMAIN="$2"; shift 2;;
    --tcp-port) TCP_PORT="$2"; shift 2;;
    -h|--help) show_usage; exit 0;;
    *) echo "Unknown arg: $1"; show_usage; exit 1;;
  esac
done

# Prompt if missing
if [ -z "$PRIVATE_DOMAIN" ]; then
  read -rp "Enter RAILWAY_PRIVATE_DOMAIN (e.g. xyz.internal.railway): " PRIVATE_DOMAIN
fi
if [ -z "$TCP_DOMAIN" ]; then
  read -rp "Enter RAILWAY_TCP_PROXY_DOMAIN (optional, leave empty if unknown): " TCP_DOMAIN
fi
if [ -z "$TCP_PORT" ]; then
  read -rp "Enter RAILWAY_TCP_PROXY_PORT (optional, default 3306): " TCP_PORT
  TCP_PORT=${TCP_PORT:-3306}
fi

# Sensitive secret from your earlier message
MYSQL_ROOT_PASSWORD="zEGPYQzuMqMyjVaKeUcJNkMlBJBqPKZz"
MYSQL_DATABASE="railway"
MYSQLPORT="3306"
MYSQLUSER="root"

# Verify railway CLI
if ! command -v railway >/dev/null 2>&1; then
  echo "railway CLI not found. Install from https://railway.app or run in environment with Railway CLI."
  exit 1
fi

echo "Logging into Railway (opens browser if necessary)..."
railway login || true

# Attempt to link (will prompt if not linked)
echo "Linking project directory to Railway (if not already linked)..."
railway link || true

# helper to set variables with fallbacks for different CLI versions
set_var(){
  local key="$1"; local value="$2"; local secret_flag="$3"
  # Try `railway variables set KEY VALUE [--secret]`
  if railway variables set "$key" "$value" ${secret_flag:+--secret} >/dev/null 2>&1; then
    echo "Set $key"
    return 0
  fi
  # Try `railway env set KEY VALUE [--secret]` (older/newer variants)
  if railway env set "$key" "$value" >/dev/null 2>&1; then
    echo "Set $key (using railway env set)"
    return 0
  fi
  # If both fail, echo command for manual copy
  echo "Failed to set $key via CLI. Please set it manually in Railway dashboard: $key"
  return 1
}

# Set variables
set_var MYSQL_DATABASE "$MYSQL_DATABASE" ""
set_var MYSQL_ROOT_PASSWORD "$MYSQL_ROOT_PASSWORD" "--secret"
set_var MYSQLPORT "$MYSQLPORT" ""
set_var MYSQLUSER "$MYSQLUSER" ""
set_var MYSQLHOST "$PRIVATE_DOMAIN" ""
set_var MYSQLPASSWORD "$MYSQL_ROOT_PASSWORD" "--secret"

# Construct URLs
MYSQL_URL_VAL="mysql://${MYSQLUSER}:${MYSQL_ROOT_PASSWORD}@${PRIVATE_DOMAIN}:3306/${MYSQL_DATABASE}"
set_var MYSQL_URL "$MYSQL_URL_VAL" ""

if [ -n "$TCP_DOMAIN" ]; then
  MYSQL_PUBLIC_URL_VAL="mysql://${MYSQLUSER}:${MYSQL_ROOT_PASSWORD}@${TCP_DOMAIN}:${TCP_PORT}/${MYSQL_DATABASE}"
  set_var MYSQL_PUBLIC_URL "$MYSQL_PUBLIC_URL_VAL" ""
fi

# Optional aliases
set_var MYSQLDATABASE "$MYSQL_DATABASE" ""
set_var MYSQLHOST "$PRIVATE_DOMAIN" ""
set_var MYSQLPASSWORD "$MYSQL_ROOT_PASSWORD" "--secret"
set_var MYSQLPORT "$MYSQLPORT" ""
set_var MYSQLUSER "$MYSQLUSER" ""

echo "All done. Current Railway variables:"
railway variables || railway env list || true

echo "Triggering a deploy (optional)"
read -rp "Run 'railway up' to deploy now? (y/N): " run_now
if [[ "$run_now" =~ ^[Yy]$ ]]; then
  railway up
fi

echo "Script finished. Check Railway logs for DB connection messages."
