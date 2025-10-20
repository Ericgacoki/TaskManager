#!/bin/bash

# Detect current machine IP
CURRENT_IP=$(ip route get 1.1.1.1 | grep -oP 'src \K[^ ]+' | head -1)

YELLOW='\033[1;33m'
GREEN='\033[1;32m'
NC='\033[0m' # No Color

echo -e "\n${YELLOW}Device IP: $CURRENT_IP${NC}"
echo "--------------------------"
echo -e "${GREEN}BASE_URL Updated to http://$CURRENT_IP:3000/${NC}\n"

# Replace only the device flavor BASE_URL
sed -i "/create(\"device\")/,/}/s|http://[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+:3000|http://$CURRENT_IP:3000|g" app/build.gradle.kts
