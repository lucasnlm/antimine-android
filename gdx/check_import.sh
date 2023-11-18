#!/bin/bash

# Check if a file was provided as argument
if [ $# -eq 0 ]; then
  echo "Error: Please provide a filename as argument."
  exit 1
fi

# Check if the file exists
if [ ! -f "$1" ]; then
  echo "Error: File '$1' not found."
  exit 1
fi

# Check if the file contains the string in an unique line
if grep -xq "project.tasks.preBuild.dependsOn(\"copyAndroidNatives\")" "$1"; then
  exit 0
else
  exit 1
fi
