#!/bin/bash

# find out the last tag
LASTTAG=$(git describe --tags --abbrev=0)

# lists all commit messages since the last tag
git log ${LASTTAG}..HEAD --format=format:%s

# print last tag for verification purposes
echo
echo ${LASTTAG}
